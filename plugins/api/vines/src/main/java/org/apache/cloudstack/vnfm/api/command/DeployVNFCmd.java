package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.vnfm.Vnf;
import com.cloud.vnfm.VnfVO;

@APICommand(name = "deployVnf", description = "Creates and automatically starts a VNF based on a VNF Package.", responseObject = VnfResponse.class, responseView = ResponseView.Restricted, entityType = {
        Vnf.class }, requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DeployVNFCmd extends BaseAsyncCreateCustomIdCmd {

    public static final Logger s_logger = Logger.getLogger(DeployVNFCmd.class.getName());

    private static final String s_name = "deployvnfresponse";

    @Inject
    private VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VNFP_ID, type = CommandType.STRING, required = true, description = "The VNF Package ID")
    private String vnfpId;

    @Parameter(name = ApiConstants.EMS_ID, type = CommandType.STRING, required = true, description = "The EMS ID that the VNF will be associated with")
    private String emsId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "availability zone for the virtual machine")
    private Long zoneId;

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "destination Host ID to deploy the VM to - parameter available for root admin only")
    private Long hostId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional account for the virtual machine. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "an optional domainId for the virtual machine. If the account parameter is used, domainId must also be used.")
    private Long domainId;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "Deploy vm for the project")
    private Long projectId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    public static String getResultObjectName() {
        return "vnf";
    }

    public String getVnfpId() {
        return vnfpId;
    }

    public String getEmsId() {
        return emsId;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_VM_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "starting Vm. Vm Id: " + getEntityUuid();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.VirtualMachine;
    }

    public boolean getStartVm() {
        return true;
    }

    @Override
    public long getEntityOwnerId() {
        Long accountId = _accountService.finalyzeAccountId(accountName, domainId, projectId, true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }
        return accountId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        VnfVO result = null;
        // if (getStartVm()) {
        try {
            CallContext.current().setEventDetails("Vm Id: " + getEntityUuid());
            result = _vnfManager.deployVnf(this);
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (InsufficientCapacityException ex) {
            StringBuilder message = new StringBuilder(ex.getMessage());
            if (ex instanceof InsufficientServerCapacityException) {
                if (((InsufficientServerCapacityException) ex).isAffinityApplied()) {
                    message.append(
                            ", Please check the affinity groups provided, there may not be sufficient capacity to follow them");
                }
            }
            s_logger.info(ex);
            s_logger.info(message.toString(), ex);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, message.toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // } else {
        // result = _userVmService.getUserVm(getEntityId());
        // }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (result != null) {
            VnfResponse response = new VnfResponse(result.getUuid(), result.getName(), result.getVnfpId(),
                    result.getEmsId(), result.getCreated());
            response.setObjectName("vnf");
            // UserVmResponse response = _responseGenerator
            // .createUserVmResponse(ResponseView.Restricted, "virtualmachine",
            // result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to deploy vm uuid:" + getEntityUuid());
        }
    }

    @Override
    public void create() throws ResourceAllocationException {
        try {
            UserVm vm = _vnfManager.createVnfVm(this);
            if (vm == null) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create VNF VM");
            }
            VnfVO vnf = _vnfManager.createVnfRecord(vnfpId, emsId, vm.getId());
            if (vnf != null) {
                setEntityId(vnf.getId());
                setEntityUuid(vnf.getUuid());
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create VNF");
            }
            /*
             * if (vm != null) { setEntityId(vm.getId()); setEntityUuid(vm.getUuid()); }
             * else { throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR,
             * "Failed to deploy VNF"); }
             */
        } catch (InsufficientCapacityException ex) {
            s_logger.info(ex);
            s_logger.trace(ex.getMessage(), ex);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        } catch (ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (ResourceAllocationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_ALLOCATION_ERROR, ex.getMessage());
        }
    }
}