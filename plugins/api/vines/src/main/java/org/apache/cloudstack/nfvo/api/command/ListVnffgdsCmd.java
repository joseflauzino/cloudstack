package org.apache.cloudstack.nfvo.api.command;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.nfvo.NFVOrchestrator;
import org.apache.cloudstack.nfvo.api.response.VnffgdResponse;
import org.apache.log4j.Logger;

@APICommand(name = "listVnffgds", description = "List the VNF Forward Graph Descriptors", responseObject = VnffgdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListVnffgdsCmd extends BaseCmd {

    @Inject
    NFVOrchestrator _nfvOrchestrator;

    public static final Logger s_logger = Logger.getLogger(ListVnffgdsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnffgdsresponse";

    @Parameter(name = "vnffgdid", type = CommandType.STRING, description = "The VNFFGD ID")
    private String vnffgdId;

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

    public String getVnffgdId() {
        return vnffgdId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        List<VnffgdResponse> responses = _nfvOrchestrator.listVnffgd(this);
        ListResponse<VnffgdResponse> listReponse = new ListResponse<VnffgdResponse>();
        listReponse.setResponses(responses);
        listReponse.setResponseName(getCommandName());
        this.setResponseObject(listReponse);
    }

}