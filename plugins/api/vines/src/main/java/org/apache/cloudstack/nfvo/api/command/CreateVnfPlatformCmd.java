package org.apache.cloudstack.nfvo.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.nfvo.NFVOrchestrator;
import org.apache.cloudstack.nfvo.api.response.VnfPlatformResponse;
import org.apache.cloudstack.nfvo.vo.VnfPlatformVO;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.user.Account;

@APICommand(name = "createVnfPlatform", description = "Creates a VNF Platform", responseObject = VnfPlatformResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class CreateVnfPlatformCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = Logger.getLogger(CreateVnfPlatformCmd.class.getName());

    @Inject
    NFVOrchestrator _nfvOrchestrator;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "createvnfplatformresponse";

    @Parameter(name = "vnfplatformname", type = CommandType.STRING, description = "The VNF Platform name")
    private String vnfPlatformName;

    @Parameter(name = "description", type = CommandType.STRING, description = "A short description.")
    private String description;

    @Parameter(name = "drivername", type = CommandType.STRING, description = "The VNF Platform Driver name.")
    private String driverName;

    @Parameter(name = "defaultnic", type = CommandType.STRING, description = "The default NIC of the VNFs that will be instantiated using this platform. Possible values: mgmtInterface, firstTrafficInterface.")
    private String defaultnic;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }

    public Account getEntityOwner() {
        return CallContext.current().getCallingAccount();
    }

    public String getVnfPlatformName() {
        return this.vnfPlatformName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDriverName() {
        return this.driverName;
    }

    public String getDefaultNic() {
        return this.defaultnic;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VNF_PLATFORM_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Creating a VNF PLatform";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        VnfPlatformResponse response = _nfvOrchestrator.createVnfPlatform(this);
        response.setObjectName("vnfplatform");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public void create() throws ResourceAllocationException {
        VnfPlatformVO vnfPlatformVO = _nfvOrchestrator.createVnfPlatformRecord(vnfPlatformName, description, driverName,
                defaultnic);
        if (vnfPlatformVO != null) {
            setEntityId(vnfPlatformVO.getId());
            setEntityUuid(vnfPlatformVO.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a VNF Platform");
        }
    }
}