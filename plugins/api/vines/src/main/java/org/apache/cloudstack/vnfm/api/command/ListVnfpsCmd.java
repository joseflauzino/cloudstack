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
import org.apache.cloudstack.vnfm.api.response.VnfpResponse;
import org.apache.log4j.Logger;

@APICommand(name = "listVnfps", description = "List the VNF Packages", responseObject = VnfpResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListVnfpsCmd extends BaseCmd {

    @Inject
    private VNFManager _vnfManager;

    public static final Logger s_logger = Logger.getLogger(ListVnfpsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnfpsresponse";

    @Parameter(name = "id", type = CommandType.STRING, description = "the VNF Package ID", required = false)
    private String vnfpId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public String getVnfpId() {
        return this.vnfpId;
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
        List<VnfpResponse> responses = _vnfManager.listVnfp(this);
        ListResponse<VnfpResponse> listReponse = new ListResponse<VnfpResponse>();
        listReponse.setResponses(responses);
        listReponse.setResponseName(getCommandName());
        this.setResponseObject(listReponse);
    }
}