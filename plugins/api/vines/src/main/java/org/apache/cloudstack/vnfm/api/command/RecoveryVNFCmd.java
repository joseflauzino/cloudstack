package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.vnfm.Vnf;

@APICommand(name = "recoveryVnf", description = "Recovery a crashed VNF", responseObject = VnfResponse.class, responseView = ResponseView.Restricted, entityType = {
        Vnf.class }, requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RecoveryVNFCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(RecoveryVNFCmd.class.getName());

    private static final String s_name = "recoveryvnfresponse";

    @Inject
    private VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = "id", type = CommandType.STRING, required = true, description = "The VNF ID")
    private String id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    public static String getResultObjectName() {
        return "vnf";
    }

    public String getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_RECOVERY_VNF;
    }

    @Override
    public String getEventDescription() {
        return "Recovering a VNF";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.VirtualMachine;
    }

    public boolean getStartVm() {
        return true;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws InsufficientCapacityException, ResourceUnavailableException {
        VnfResponse response = _vnfManager.recoveryVnf(this);
        response.setObjectName("vnf");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}