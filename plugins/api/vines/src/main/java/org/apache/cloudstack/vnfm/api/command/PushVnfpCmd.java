package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiArgValidator;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.EMSOperationResponse;
import org.apache.cloudstack.vnfm.api.response.PushVnfpCmdResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;

@APICommand(name = "pushVnfp", description = "Push the VNF Package to VNF", responseObject = PushVnfpCmdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class PushVnfpCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(PushVnfpCmd.class.getName());

    @Inject
    VNFManager _vnfpManager;

    private static final String s_name = "pushvnfpresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VNF_ID, description = "The VNFP to push to VNF", required = true, validations = {
            ApiArgValidator.NotNullOrEmpty })
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
        return EventTypes.EVENT_PUSH_VNFP;
    }

    @Override
    public String getEventDescription() {
        return "Pushing the VNFP to VNF VM";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        EMSOperationResponse result = _vnfpManager.pushVnfp(vnfId);
        if (result.isSuccess()) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            response.setObjectName("pushvnfp");
            response.setDisplayText("VNF Package successfully pushed");
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to push VNFP");
        }
    }
}