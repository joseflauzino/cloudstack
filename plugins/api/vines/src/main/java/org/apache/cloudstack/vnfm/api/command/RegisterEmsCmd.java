package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiArgValidator;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.EmsResponse;
import org.apache.cloudstack.vnfm.dao.EmsDao;
import org.apache.cloudstack.vnfm.vo.EmsVO;
import org.apache.log4j.Logger;

import com.cloud.utils.exception.CloudRuntimeException;

@APICommand(name = "registerEms", description = "Register an Ems in the VNFM", responseObject = EmsResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class RegisterEmsCmd extends BaseCmd {
    @Inject
    private EmsDao _emsDao;

    @Inject
    private VNFManager _vnfManager;

    public static final Logger s_logger = Logger.getLogger(RegisterEmsCmd.class.getName());

    private static final String s_name = "registeremsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the EMS name", required = true, validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String name;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = true, description = "the EMS Public IP.", validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String ip;

    @Parameter(name = ApiConstants.PORT, type = CommandType.STRING, required = true, description = "the TCP port on which the EMS is listening.", validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String port;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
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
        EmsVO vo = _vnfManager.registerEms(this);
        vo = _emsDao.findByUuid(vo.getUuid());
        if (vo == null) {
            throw new CloudRuntimeException("Unable to register the EMS " + name);
        }
        EmsResponse response = new EmsResponse(vo.getUuid(), vo.getName(), vo.getIp(), vo.getPort(), vo.getCreated());
        response.setObjectName("ems");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}