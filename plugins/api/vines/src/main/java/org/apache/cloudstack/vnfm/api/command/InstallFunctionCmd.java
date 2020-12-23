package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.EMSOperationResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;

@APICommand(name = "installFunction", description = "Install the Function into a VNF", responseObject = EMSOperationResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class InstallFunctionCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(InstallFunctionCmd.class.getName());
    private static final String s_name = "lifecycleoperationresponse";

    @Inject
    VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = "vnfid", type = CommandType.STRING, description = "The VNF ID")
    private String vnfId;

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

    public String getVnfId() {
        return this.vnfId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_INSTALL_FUNCTION;
    }

    @Override
    public String getEventDescription() {
        return "Installing the Network Function";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        EMSOperationResponse response = _vnfManager.installFunction(this);
        response.setObjectName("operation");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}