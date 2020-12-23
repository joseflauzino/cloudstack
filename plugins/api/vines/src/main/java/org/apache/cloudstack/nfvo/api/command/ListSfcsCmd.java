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
import org.apache.cloudstack.nfvo.api.response.SfcResponse;
import org.apache.log4j.Logger;

@APICommand(name = "listSfcs", description = "List the SFCs", responseObject = SfcResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListSfcsCmd extends BaseCmd {

    @Inject
    private NFVOrchestrator _nfvOchestrator;

    public static final Logger s_logger = Logger.getLogger(ListSfcsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listsfcresponse";

    @Parameter(name = "sfcid", type = CommandType.STRING, description = "the SFC ID")
    private String sfcId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public String getSfcId() {
        return sfcId;
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
        List<SfcResponse> responses = _nfvOchestrator.listSfc(this);
        ListResponse<SfcResponse> listReponse = new ListResponse<SfcResponse>();
        listReponse.setResponses(responses);
        listReponse.setResponseName(getCommandName());
        this.setResponseObject(listReponse);
    }
}