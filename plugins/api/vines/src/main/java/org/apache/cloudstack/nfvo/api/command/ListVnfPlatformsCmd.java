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
import org.apache.cloudstack.nfvo.api.response.VnfPlatformResponse;
import org.apache.log4j.Logger;

@APICommand(name = "listVnfPlatform", description = "List the VNF Platform", responseObject = VnfPlatformResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListVnfPlatformsCmd extends BaseCmd {

    @Inject
    private NFVOrchestrator _nfvOchestrator;

    public static final Logger s_logger = Logger.getLogger(ListVnfPlatformsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnfplatformresponse";

    @Parameter(name = "vnfplatformid", type = CommandType.STRING, description = "the VNF Platform ID")
    private String vnfPlatformId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public String getVnfPlatformId() {
        return vnfPlatformId;
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
        List<VnfPlatformResponse> responses = _nfvOchestrator.listVnfPlatform(this);
        ListResponse<VnfPlatformResponse> listReponse = new ListResponse<VnfPlatformResponse>();
        listReponse.setResponses(responses);
        listReponse.setResponseName(getCommandName());
        this.setResponseObject(listReponse);
    }
}