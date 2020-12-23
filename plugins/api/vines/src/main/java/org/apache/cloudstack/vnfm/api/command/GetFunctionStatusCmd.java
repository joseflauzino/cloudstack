package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.EMSOperationResponse;
import org.apache.log4j.Logger;

@APICommand(name = "getFunctionStatus", description = "Get Network Function status", responseObject = EMSOperationResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class GetFunctionStatusCmd extends BaseCmd {

    public static final Logger s_logger = Logger.getLogger(GetFunctionStatusCmd.class.getName());
    private static final String s_name = "getfunctionstatusresponse";

    @Inject
    VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = "vnfid", type = CommandType.STRING, description = "the VNF ID")
    private String vnfId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getVnfId() {
        return this.vnfId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        EMSOperationResponse response = _vnfManager.getFunctionStatus(this);
        response.setObjectName("operation");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

}