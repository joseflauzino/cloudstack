package org.apache.cloudstack.nfvo.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiArgValidator;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.nfvo.NFVOrchestrator;
import org.apache.cloudstack.nfvo.api.response.VnffgdResponse;
import org.apache.cloudstack.nfvo.vo.VnffgdVO;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;

@APICommand(name = "createVnffgd", description = "Creates a VNF Forward Graph Descriptor", responseObject = VnffgdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class CreateVnffgdCmd extends BaseAsyncCreateCmd {

    @Inject
    NFVOrchestrator _nfvOrchestrator;

    public static final Logger s_logger = Logger.getLogger(CreateVnffgdCmd.class.getName());

    private static final String s_name = "vnffgdcreateresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = "url", type = CommandType.STRING, required = true, description = "the URL to where the VNFGD is hosted. The URL must be encoded. Support HTTP and HTTPS.", validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String url;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getUrl() {
        return url;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VNFFGD_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Creating a VNFFGD";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        VnffgdResponse response = _nfvOrchestrator.createVnffgd(this);
        response.setObjectName("vnffgd");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public void create() throws ResourceAllocationException {
        VnffgdVO vnffgdVO = _nfvOrchestrator.createVnffgdRecord(url);
        if (vnffgdVO != null) {
            setEntityId(vnffgdVO.getId());
            setEntityUuid(vnffgdVO.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create VNFFGD");
        }
    }
}