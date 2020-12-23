package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.vnfm.Vnf;

@APICommand(name = "scaleVnf", description = "Scale a VNF", responseObject = VnfResponse.class, responseView = ResponseView.Restricted, entityType = {
        Vnf.class }, requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ScaleVNFCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(ScaleVNFCmd.class.getName());

    private static final String s_name = "scalevnfresponse";

    @Inject
    private VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = "id", type = CommandType.STRING, required = true, description = "The VNF ID")
    private String id;

    @Parameter(name = "serviceofferingid", type = CommandType.STRING, required = true, description = "The new template ID")
    private String serviceOfferingId;

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

    public String getId() {
        return id;
    }

    public String getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_RECOVERY_VNF;
    }

    @Override
    public String getEventDescription() {
        return "Scaling a VNF";
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
        return 0;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        VnfResponse response = null;
        try {
            response = _vnfManager.scaleVnf(this);
        } catch (ConcurrentOperationException | ResourceUnavailableException | ManagementServerException
                | VirtualMachineMigrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        response.setObjectName("vnf");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}