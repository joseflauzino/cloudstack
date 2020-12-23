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
import org.apache.cloudstack.vnfm.api.response.VnfpResponse;
import org.apache.cloudstack.vnfm.dao.VnfpDao;
import org.apache.cloudstack.vnfm.vo.VnfpVO;
import org.apache.log4j.Logger;

import com.cloud.utils.exception.CloudRuntimeException;

@APICommand(name = "createVnfp", description = "Creates a VNF Package", responseObject = VnfpResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class CreateVnfpCmd extends BaseCmd {
    @Inject
    private VnfpDao _vnfpDao;

    @Inject
    private VNFManager _vnfManager;

    public static final Logger s_logger = Logger.getLogger(CreateVnfpCmd.class.getName());

    private static final String s_name = "createvnfpresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the VNF Package name", required = true, validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String name;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, description = "the URL to where the VNF Package is hosted. The URL must be encoded. Support HTTP and HTTPS.", validations = {
            ApiArgValidator.NotNullOrEmpty })
    private String url;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

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

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        VnfpVO vo = _vnfManager.createVnfp(this);
        vo = _vnfpDao.findByUuid(vo.getUuid());
        if (vo == null) {
            throw new CloudRuntimeException("Unable to create a VNFP " + name);
        }
        VnfpResponse response = new VnfpResponse(vo.getUuid(), vo.getName(), vo.getUrlDecoded(), vo.getCreated());
        response.setObjectName("vnfp");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}