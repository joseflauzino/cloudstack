package org.apache.cloudstack.vnfm.api.command;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.ListVnfsCmdResponse;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.log4j.Logger;

@APICommand(name = "listVnfs", description = "List the VNFs instances", responseObject = ListVnfsCmdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListVnfsCmd extends BaseCmd {
    @Inject
    private VNFManager _vnfManager;

    public static final Logger s_logger = Logger.getLogger(ListVnfsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnfsresponse";

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

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public void execute() {
        List<VnfResponse> responses = _vnfManager.listVnf(this);
        ListResponse<VnfResponse> listReponse = new ListResponse<VnfResponse>();
        listReponse.setResponses(responses);
        listReponse.setResponseName(getCommandName());
        this.setResponseObject(listReponse);
    }
}