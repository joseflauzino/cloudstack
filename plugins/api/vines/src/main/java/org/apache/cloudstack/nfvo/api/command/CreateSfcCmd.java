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
import org.apache.cloudstack.nfvo.api.response.SfcResponse;
import org.apache.cloudstack.nfvo.vo.SfcVO;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.user.Account;

@APICommand(name = "createSfc", description = "Creates a Service Function Chain", responseObject = SfcResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class CreateSfcCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = Logger.getLogger(CreateSfcCmd.class.getName());

    @Inject
    NFVOrchestrator _nfvOrchestrator;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "createsfcresponse";

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

    public Account getEntityOwner() {
        return CallContext.current().getCallingAccount();
    }

    public String getVnffgdId() {
        return this.vnffgdId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SFC_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Creating a SFC";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        SfcResponse response = _nfvOrchestrator.createSfc(this);
        response.setObjectName("sfc");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public void create() throws ResourceAllocationException {
        SfcVO sfcVO = _nfvOrchestrator.createSfcRecord(vnffgdId);
        if (sfcVO != null) {
            setEntityId(sfcVO.getId());
            setEntityUuid(sfcVO.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create SFC");
        }
    }
}