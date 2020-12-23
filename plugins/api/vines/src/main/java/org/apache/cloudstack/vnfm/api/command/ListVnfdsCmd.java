package org.apache.cloudstack.vnfm.api.command;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.vnfm.api.response.ListVnfdsCmdResponse;
import org.apache.cloudstack.vnfm.dao.VnfpDao;
import org.apache.cloudstack.vnfm.vo.VnfpVO;
import org.apache.log4j.Logger;

import com.cloud.utils.exception.CloudRuntimeException;

@APICommand(name = "listVnfds", description = "List the VNF Descriptors", responseObject = ListVnfdsCmdResponse.class, includeInApiDoc = true, authorized = {
        RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User })
public class ListVnfdsCmd extends BaseCmd {
    @Inject
    private VnfpDao _vnfpDao;

    public static final Logger s_logger = Logger.getLogger(ListVnfdsCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    private static final String s_name = "listvnfdresponse";

    @Parameter(name = "vnfpid", type = CommandType.STRING, description = "The VNFP ID which VNFD belongs")
    private String vnfpId;

    @Override
    public void execute() {
        ListVnfdsCmdResponse response = new ListVnfdsCmdResponse();
        if (vnfpId != null) {
            VnfpVO vo = _vnfpDao.findByUuid(vnfpId);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find VNFP for ID: " + vnfpId);
            }
            response.setVnfdData(vo.getUuid());
        } else {
            List<VnfpVO> vnfps = _vnfpDao.listAll();
            if (vnfps == null) {
                throw new CloudRuntimeException("Unable to list VNF Packages");
            }
            response.setVnfdData(vnfps);
        }

        response.setObjectName("listvnfd"); // the inner part of the json structure
        response.setResponseName(getCommandName()); // the outer part of the json structure
        this.setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }
}