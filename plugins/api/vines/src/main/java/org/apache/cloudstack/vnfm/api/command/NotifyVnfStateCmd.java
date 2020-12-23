package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.ListVnfsCmdResponse;
import org.apache.cloudstack.vnfm.api.response.VnfStateNotificationResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;

@APICommand(name = "notifyVnfState", description = "Notify the VNFM of any change in the value of a VNF State", responseObject = ListVnfsCmdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class NotifyVnfStateCmd extends BaseAsyncCreateCmd {
    @Inject
    private VNFManager _vnfManager;

    public static final Logger s_logger = Logger.getLogger(NotifyVnfStateCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnfsresponse";

    @Parameter(name = "subscriptionid", type = CommandType.STRING, required = true, description = "The subscription ID")
    private String subscriptionId;

    @Parameter(name = "vnfstate", type = CommandType.STRING, required = true, description = "The VNF State. Possible values: active and inactive")
    private String vnfState;

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

    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    public String getVnfState() {
        return this.vnfState;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_HANDLE_NOTIFICATION;
    }

    @Override
    public String getEventDescription() {
        return "Handling VNF State notification";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public void execute() {
        VnfStateNotificationResponse response = _vnfManager.handleVnfStateNotification(this);
        this.setResponseObject(response);
    }

    @Override
    public void create() throws ResourceAllocationException {
        setEntityUuid("c851c0dc-553a-4f10-a72a-53a47baa6526");
    }
}