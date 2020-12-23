package org.apache.cloudstack.nfvo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.nfvo.api.command.CreateSfcCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnfPlatformCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnffgdCmd;
import org.apache.cloudstack.nfvo.api.command.ListSfcsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnfPlatformsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnffgdsCmd;
import org.apache.cloudstack.nfvo.api.response.SfcResponse;
import org.apache.cloudstack.nfvo.api.response.VnfPlatformResponse;
import org.apache.cloudstack.nfvo.api.response.VnffgdResponse;
import org.apache.cloudstack.nfvo.dao.SfcDao;
import org.apache.cloudstack.nfvo.dao.VnfPlatformDao;
import org.apache.cloudstack.nfvo.dao.VnffgdDao;
import org.apache.cloudstack.nfvo.vo.SfcVO;
import org.apache.cloudstack.nfvo.vo.VnfPlatformVO;
import org.apache.cloudstack.nfvo.vo.VnffgdVO;
import org.apache.cloudstack.util.FirewallRuleUtil;
import org.apache.cloudstack.util.VinesUtil;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.EMSOperationResponse;
import org.springframework.stereotype.Component;

import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.RulesService;
import com.cloud.user.Account;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.vines.Classifier;
import com.cloud.utils.vines.GetToscaVnfdUtil;
import com.cloud.utils.vines.GetToscaVnffgdUtil;
import com.cloud.utils.vines.Path;
import com.cloud.utils.vines.ToscaVnfd;
import com.cloud.utils.vines.ToscaVnffgd;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vnfm.VnfVO;
import com.cloud.vnfm.dao.VnfDao;

@Component
@DB
public class NFVOrchestratorImpl implements NFVOrchestrator {
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private NicDao _nicDao;
    @Inject
    private VnfDao _vnfDao;
    @Inject
    private VMInstanceDao _vmInstanceDao;
    @Inject
    private VnffgdDao _vnffgdDao;
    @Inject
    private SfcDao _sfcDao;
    @Inject
    private VnfPlatformDao _vnfPlatformDao;
    @Inject
    private DataCenterDao _dataCenterDao;

    @Inject
    private IpAddressManager _ipAddrMgr;
    @Inject
    private RulesService _rulesService;
    @Inject
    private FirewallService _firewallService;

    @Inject
    VNFManager _vnfManager;

    private NetworkVO findNetworkByCidr(String network) {
        List<NetworkVO> netList = _networkDao.listAll();
        for (NetworkVO n : netList) {
            if (n.getCidr() != null && n.getCidr().equals(network)) {
                return n;
            }
        }
        return null;
    }

    public String findRouterIP(String guestGatewayIP) {
        List<NicVO> nics = _nicDao.listAll();
        NicVO nic = null;
        for (NicVO n : nics) {
            long nInstanceId = 0;
            try {
                nInstanceId = n.getInstanceId();
            } catch (Exception e) {
                nInstanceId = 0;
            }
            if (nInstanceId != 0 && n.getReserver().equals("DirectNetworkGuru")
                    && n.getIPv4Address().equals(guestGatewayIP)) {
                nic = n;
            }
        }
        if (nic == null) {
            throw new CloudRuntimeException("Unable to identify the gateway of the network described in VNFFGD");
        }
        long routerId = nic.getInstanceId();
        for (NicVO n : nics) {
            long nInstanceId = 0;
            try {
                nInstanceId = n.getInstanceId();
            } catch (Exception e) {
            }
            if (nInstanceId != 0 && nInstanceId == routerId && n.getReserver().equals("ControlNetworkGuru")) {
                return n.getIPv4Address();
            }
        }
        return "";
    }

    public String getVnfVmIp(long vnfVmId, String guestNetworkGateway) {
        for (NicVO n : _nicDao.listAll()) {
            long nInstanceId = 0;
            try {
                nInstanceId = n.getInstanceId();
            } catch (Exception e) {
            }
            if (nInstanceId != 0 && nInstanceId == vnfVmId && n.getIPv4Gateway().equals(guestNetworkGateway)) {
                return n.getIPv4Address();
            }
        }
        return "";
    }

    public VMInstanceVO findVnfVmInstance(String vnfUuid) {
        VnfVO vnfVO = _vnfDao.findByUuid(vnfUuid);
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to identify VNF with UUID " + vnfUuid);
        }
        VMInstanceVO vnfVm = _vmInstanceDao.findById(vnfVO.getVmId());
        if (vnfVm == null) {
            throw new CloudRuntimeException("Unable to identify VNF VM with ID " + vnfVO.getVmId());
        }
        return vnfVm;
    }

    private IpAddress associateIpAddr(Account owner, long zoneId) {
        Account caller = CallContext.current().getCallingAccount();
        long callerUserId = CallContext.current().getCallingUserId();
        DataCenter zone = _dataCenterDao.findById(zoneId);
        IpAddress ip = null;
        try {
            ip = _ipAddrMgr.allocateIp(owner, false, caller, callerUserId, zone, true, "");
        } catch (InsufficientAddressCapacityException e) {
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            e.printStackTrace();
        } catch (ResourceAllocationException e) {
            e.printStackTrace();
        }
        return ip;
    }

    private IpAddress associateIPToNetwork(long ipId, long networkId) {
        Network network = _networkDao.findById(networkId);
        if (network == null) {
            throw new InvalidParameterValueException("Invalid network id is given how public traffic");
        }
        if (network.getVpcId() != null) {
            throw new InvalidParameterValueException("Can't assign ip to the network directly when network belongs"
                    + " to VPC.Specify vpcId to associate ip address to VPC");
        }
        IpAddress ip = null;
        try {
            ip = _ipAddrMgr.associateIPToGuestNetwork(ipId, networkId, true);
        } catch (InsufficientAddressCapacityException e) {
            e.printStackTrace();
        } catch (ResourceAllocationException e) {
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
        return ip;
    }

    private FirewallRule createFirewallRule(Long ipAddressId, String protocol, Integer publicStartPort,
            Integer publicEndPort, List<String> cidrlist, Integer icmpType, Integer icmpCode, long accountId,
            IpAddress ip) {
        FirewallRuleUtil rule = new FirewallRuleUtil(ipAddressId, protocol, publicStartPort, publicEndPort, cidrlist,
                icmpType, icmpCode, accountId, ip);
        FirewallRule result = null;
        try {
            result = _firewallService.createIngressFirewallRule(rule);
        } catch (NetworkRuleConflictException ex) {
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage(), ex);
        }
        return result;
    }

    private String toIpProtoName(int protoNumber) {
        if (protoNumber == 6) {
            return "TCP";
        } else if (protoNumber == 17) {
            return "UDP";
        } else if (protoNumber == 1) {
            return "ICMP";
        } else {
            throw new CloudRuntimeException("Unable to convert ip proto number to name");
        }
    }

    public void applySfcFirewallRules(FirewallRule rule, Account caller) {
        boolean success = false;
        try {
            success = _firewallService.applyIngressFwRules(rule.getSourceIpAddressId(), caller);
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        } finally {
            if (!success) {
                _firewallService.revokeIngressFwRule(rule.getId(), true);
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create firewall rule");
            }
        }
    }

    public FirewallRule createSfcFirewallRules(NetworkVO trafficNetwork, ToscaVnffgd vnffgd, VMInstanceVO lastVnfVm,
            String lastVnfIP) {
        // Reserves and associates a public IP with the last VNF
        IpAddress lastVnfpublicIp = associateIpAddr(CallContext.current().getCallingAccount(),
                trafficNetwork.getDataCenterId());
        long lastVnfpublicIpId = lastVnfpublicIp.getId();
        lastVnfpublicIp = associateIPToNetwork(lastVnfpublicIpId, trafficNetwork.getId());
        try {
            boolean result = _rulesService.enableStaticNat(lastVnfpublicIpId, lastVnfVm.getId(), trafficNetwork.getId(),
                    lastVnfIP);
            if (!result) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to enable static NAT");
            }
        } catch (NetworkRuleConflictException ex) {
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }

        // Creates the firewall rules
        List<Classifier> sfcClassifier = vnffgd.getClassifier();
        List<String> cidrlist = new ArrayList<String>();
        cidrlist.add("0.0.0.0/0");
        FirewallRule rule = null;
        for (Classifier c : sfcClassifier) {
            String protocol = toIpProtoName(Integer.parseInt(c.getProtocol()));
            String port = c.getPort();
            if (protocol.equals("ICMP")) { // icmp
                rule = createFirewallRule(lastVnfpublicIp.getId(), protocol, Integer.parseInt(c.getPort()),
                        Integer.parseInt(c.getPort()), cidrlist, (Integer) 0, (Integer) 0, (long) 0, lastVnfpublicIp);
            } else { // others protocols
                rule = createFirewallRule(lastVnfpublicIp.getId(), protocol, Integer.parseInt(c.getPort()),
                        Integer.parseInt(c.getPort()), cidrlist, null, null, (long) 0, lastVnfpublicIp);
            }
        }
        return rule;
    }

    public String getVnfMgmtInterfaceUuid(VnfVO vnfVO) {
        String vnfpUuid = vnfVO.getVnfpId();
        ToscaVnfd vnfd = GetToscaVnfdUtil.readVnfdFile(vnfpUuid);
        return vnfd.getManagementInterfaceUuid();
    }

    public String findGuestNetworkGatewayIp(String networkUuid) {
        NetworkVO network = _networkDao.findByUuid(networkUuid);
        if (network == null) {
            throw new CloudRuntimeException("Unable to find network with UUID " + networkUuid);
        }
        return network.getGateway();
    }

    @ActionEvent(eventType = EventTypes.EVENT_SFC_CREATE, eventDescription = "Creating SFC", async = true)
    public SfcResponse createSfc(CreateSfcCmd cmd) {
        SfcVO sfcVO = _sfcDao.findById(cmd.getEntityId());
        if (sfcVO == null) {
            throw new CloudRuntimeException(
                    "Unable to create the SFC. Entity with ID " + cmd.getEntityId() + " not found.");
        }

        ToscaVnffgd vnffgd = GetToscaVnffgdUtil.readVnffgdFile(sfcVO.getVnffgdId());
        Account caller = cmd.getEntityOwner();

        boolean success = true;
        String urlString = "", urlParameters = "";
        EMSOperationResponse httpResponse = null;

        List<Path> sfcPath = vnffgd.getPath();
        int pathSize = sfcPath.size();

        // Identifies the management interface of the each VNF
        List<String> mgmtIfaceUuidList = new ArrayList<String>();
        for (int i = 0; i < pathSize; i++) {
            VnfVO vnfVO = _vnfDao.findByUuid(sfcPath.get(i).getVnfId());
            if (vnfVO == null) {
                throw new CloudRuntimeException("Unable to identify VNF with UUID " + sfcPath.get(i).getVnfId());
            }
            String m = getVnfMgmtInterfaceUuid(vnfVO);
            if (m.equals("")) {
                throw new CloudRuntimeException("Unable to detect VNF management interface");
            }
            mgmtIfaceUuidList.add(m);
        }
        if (mgmtIfaceUuidList.isEmpty()) {
            throw new CloudRuntimeException("Unable to detect VNF management interfaces");
        }

        // Checks if all VNFs belong to the same management network
        String mgmtIfaceUuid = mgmtIfaceUuidList.get(0);
        boolean isSameIfaces = true;
        for (int i = 1; i < mgmtIfaceUuidList.size(); i++) {
            if (!mgmtIfaceUuid.equals(mgmtIfaceUuidList.get(i))) {
                isSameIfaces = false;
                break;
            }
        }
        if (!isSameIfaces) {
            throw new CloudRuntimeException("At least one VNF management interface is different from the others");
        }

        // Management Network: gets gateway IP and management IP (link local address) of
        // the router
        String mgmtGateway = findGuestNetworkGatewayIp(mgmtIfaceUuid);
        String mgmtRouterIp = findRouterIP(mgmtGateway);
        if (mgmtRouterIp.equals("")) {
            throw new CloudRuntimeException("Unable to get management network router IP");
        }

        // Traffic Network: gets IP, netmask and management IP (link local address) of
        // the router
        NetworkVO trafficNetwork = findNetworkByCidr(vnffgd.getNetwork());
        if (trafficNetwork == null) {
            throw new CloudRuntimeException("Unable to identify network described in VNFFGD");
        }
        String TrafficGateway = trafficNetwork.getGateway();
        System.out.println("Gateway: " + TrafficGateway);
        String TrafficRouterIp = findRouterIP(TrafficGateway);
        System.out.println("Router IP: " + TrafficRouterIp);
        if (TrafficRouterIp.equals("")) {
            throw new CloudRuntimeException("Unable to get router IP");
        }

        // First VNF
        VMInstanceVO firstVnfVm = findVnfVmInstance(sfcPath.get(0).getVnfId());
        String firstVnfIP = getVnfVmIp(firstVnfVm.getId(), TrafficGateway);
        System.out.println("First VNF IP: " + firstVnfIP);
        if (firstVnfIP.equals("")) {
            throw new CloudRuntimeException("Unable to get first VNF IP");
        }

        // Last VNF
        VMInstanceVO lastVnfVm = findVnfVmInstance(sfcPath.get(pathSize - 1).getVnfId());
        String lastVnfIP = getVnfVmIp(lastVnfVm.getId(), TrafficGateway);
        System.out.println("Last VNF IP: " + lastVnfIP);
        if (lastVnfIP.equals("")) {
            throw new CloudRuntimeException("Unable to get last VNF IP");
        }

        // Creates and apply the Firewall Rule based on classifiers
        List<Classifier> sfcClassifier = vnffgd.getClassifier();
        FirewallRule rule = createSfcFirewallRules(trafficNetwork, vnffgd, lastVnfVm, lastVnfIP);
        if (rule == null) {
            success = false;
            throw new CloudRuntimeException("Unable to create firewall rules");
        }
        applySfcFirewallRules(rule, caller);

        // Go through the N-1 VNFs that will compose the SFC, enabling forwarding
        try {
            urlString = "http://192.168.122.10:9000/api/sfc/setsfcforwarding";

            // build classifier
            String firstPart = "\"classifier\":[";
            String lastPart = "]";
            String middle = "";
            for (Classifier c : sfcClassifier) {
                middle += "{\"protocol\":\"" + c.getProtocol() + "\",\"port\":\"" + c.getPort() + "\"}";
                if (c != sfcClassifier.get(sfcClassifier.size() - 1)) {
                    middle += ",";
                }
            }
            String strSfcClassifier = firstPart + middle + lastPart;

            for (int i = 0; i < pathSize - 1; i++) {
                // Current VNF
                VMInstanceVO currentVnfVm = findVnfVmInstance(sfcPath.get(i).getVnfId());
                String currentVnfIP = getVnfVmIp(currentVnfVm.getId(), TrafficGateway);
                if (currentVnfIP.equals("")) {
                    throw new CloudRuntimeException("Unable to get current VNF IP");
                }

                // Get VNF Platform Driver of the current VNF
                VnfVO vnfVO = _vnfDao.findByUuid(sfcPath.get(i).getVnfId());
                if (vnfVO == null) {
                    throw new CloudRuntimeException("Unable to identify VNF with UUID " + sfcPath.get(i).getVnfId());
                }
                String vnfpId = vnfVO.getVnfpId();
                ToscaVnfd vnfd = GetToscaVnfdUtil.readVnfdFile(vnfpId);
                String vnfPlatformId = vnfd.getVnfPlatformId();
                VnfPlatformVO vnfPlatform = _vnfPlatformDao.findByUuid(vnfPlatformId);
                if (vnfPlatform == null) {
                    throw new CloudRuntimeException("Unable to identify VNF Platform with UUID " + vnfPlatformId);
                }
                String vnfPlatformDriverName = vnfPlatform.getDriverName();

                // Next VNF
                VMInstanceVO nextVnfVm = findVnfVmInstance(sfcPath.get(i + 1).getVnfId());
                String nextVnfIP = getVnfVmIp(nextVnfVm.getId(), TrafficGateway);
                if (nextVnfIP.equals("")) {
                    throw new CloudRuntimeException("Unable to get next VNF IP");
                }

                urlParameters = "{\"router_ip\":\"" + TrafficRouterIp + "\",\"vnf_ip\":\"" + currentVnfIP
                        + "\",\"vnf_platform\":\"" + vnfPlatformDriverName + "\",\"data\":{\"last_vnf\":\"" + lastVnfIP
                        + "\",\"next_vnf\":\"" + nextVnfIP + "\"," + strSfcClassifier + "}}";
                System.out.println(urlParameters);
                httpResponse = _vnfManager.sendOrchestrationCmd("setsfcforwarding", urlString, urlParameters, "POST");
                if (!httpResponse.isSuccess()) {
                    success = false;
                    throw new CloudRuntimeException("Unable to set SFC forwarding in VNF " + sfcPath.get(i).getVnfId());
                }
            }
        } catch (Exception e) {
            success = false;
            throw new CloudRuntimeException(e.getMessage());
        }

        // Send command to router of the traffic network to redirect the external
        // traffic to first VNF of the SFC
        try {
            urlString = "http://192.168.122.10:9000/api/sfc/setfirstvnf";
            urlParameters = "{\"first_vnf\":\"" + firstVnfIP + "\",\"last_vnf\":\"" + lastVnfIP + "\",\"router_ip\":\""
                    + TrafficRouterIp + "\"}";
            httpResponse = _vnfManager.sendOrchestrationCmd("setfirstvnf", urlString, urlParameters, "POST");
            if (!httpResponse.isSuccess()) {
                success = false;
                throw new CloudRuntimeException("Unable to set SFC forwarding in virtual router");
            }
        } catch (Exception e) {
            success = false;
            throw new CloudRuntimeException(e.getMessage());
        }

        // Response
        SfcResponse sfcResponse = null;
        if (success) {
            return new SfcResponse(sfcVO.getUuid(), sfcVO.getVnffgdId(), sfcVO.getCreated());
        }
        return sfcResponse;
    }

    public SfcVO createSfcRecord(String vnffgdUuid) {
        VnffgdVO vnffgdVO = _vnffgdDao.findByUuid(vnffgdUuid);
        if (vnffgdVO == null) {
            throw new CloudRuntimeException("Unable to find VNFFGD with ID " + vnffgdUuid);
        }
        SfcVO sfcVO = new SfcVO(vnffgdVO.getUuid());
        _sfcDao.persist(sfcVO);
        return sfcVO;
    }

    @Override
    public List<SfcResponse> listSfc(ListSfcsCmd cmd) {
        String sfcUuid = cmd.getSfcId();
        List<SfcVO> sfcs = new ArrayList<SfcVO>();
        List<SfcResponse> responses = new ArrayList<SfcResponse>();
        if (sfcUuid != null && !sfcUuid.equals("")) {
            SfcVO vo = _sfcDao.findByUuid(sfcUuid);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find SFC with ID: " + sfcUuid);
            }
            sfcs.add(vo);
        } else {
            sfcs = _sfcDao.listAll();
            if (sfcs == null) {
                throw new CloudRuntimeException("Unable to list SFCs");
            }
        }
        if (!sfcs.isEmpty()) {
            for (SfcVO sfc : sfcs) {
                SfcResponse response = new SfcResponse(sfc.getUuid(), sfc.getVnffgdId(), sfc.getCreated());
                response.setObjectName("sfc");
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VNFFGD_CREATE, eventDescription = "Creating VNFFGD", async = true)
    public VnffgdResponse createVnffgd(CreateVnffgdCmd cmd) {
        VnffgdVO vo = _vnffgdDao.findById(cmd.getEntityId());
        if (vo == null) {
            throw new CloudRuntimeException(
                    "Unable to create the VNFFGD. Entity with ID " + cmd.getEntityId() + " not found.");
        }
        // Downloads the VNFFGD
        if (!VinesUtil.downloadVnffgd(vo.getUuid(), vo.getUrl())) {
            _vnffgdDao.remove(vo.getId());
            throw new CloudRuntimeException("Unable to download the VNFFGD");
        }
        return new VnffgdResponse(vo.getUuid(), vo.getUrlDecoded(), vo.getCreated());
    }

    @Override
    public VnffgdVO createVnffgdRecord(String url) {
        VnffgdVO vnffgdVO = new VnffgdVO(url);
        _vnffgdDao.persist(vnffgdVO);
        return vnffgdVO;
    }

    @Override
    public List<VnffgdResponse> listVnffgd(ListVnffgdsCmd cmd) {
        String vnffgdUuid = cmd.getVnffgdId();
        List<VnffgdVO> vnffgds = new ArrayList<VnffgdVO>();
        List<VnffgdResponse> responses = new ArrayList<VnffgdResponse>();
        if (vnffgdUuid != null && !vnffgdUuid.equals("")) {
            VnffgdVO vo = _vnffgdDao.findByUuid(vnffgdUuid);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find VNFFGD with ID: " + vnffgdUuid);
            }
            vnffgds.add(vo);
        } else {
            vnffgds = _vnffgdDao.listAll();
            if (vnffgds == null) {
                throw new CloudRuntimeException("Unable to list VNFFGDs");
            }
        }
        if (!vnffgds.isEmpty()) {
            for (VnffgdVO vnffgd : vnffgds) {
                VnffgdResponse response = new VnffgdResponse(vnffgd.getUuid(), vnffgd.getUrlDecoded(),
                        vnffgd.getCreated());
                response.setObjectName("vnfp");
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    public VnfPlatformResponse createVnfPlatform(CreateVnfPlatformCmd cmd) {
        // TODO: enviar o driver em si para o EM. Assim nao sera preciso adicionar o
        // driver manualmente.
        VnfPlatformVO vnfPlatformVO = _vnfPlatformDao.findById(cmd.getEntityId());
        if (vnfPlatformVO == null) {
            throw new CloudRuntimeException(
                    "Unable to create the VNF Platform. Entity with ID " + cmd.getEntityId() + " not found.");
        }
        return new VnfPlatformResponse(vnfPlatformVO.getUuid(), vnfPlatformVO.getVnfPlatformName(),
                vnfPlatformVO.getDescription(), vnfPlatformVO.getDriverName(), vnfPlatformVO.getDefaultNic(),
                vnfPlatformVO.getCreated());
    }

    @Override
    public VnfPlatformVO createVnfPlatformRecord(String vnfPlatformName, String description, String driverName,
            String defaultNic) {
        VnfPlatformVO vnfPlatformVO = new VnfPlatformVO(vnfPlatformName, description, driverName, defaultNic);
        _vnfPlatformDao.persist(vnfPlatformVO);
        return vnfPlatformVO;
    }

    @Override
    public List<VnfPlatformResponse> listVnfPlatform(ListVnfPlatformsCmd cmd) {
        String vnfPlatformUuid = cmd.getVnfPlatformId();
        List<VnfPlatformVO> vnfPlatforms = new ArrayList<VnfPlatformVO>();
        List<VnfPlatformResponse> responses = new ArrayList<VnfPlatformResponse>();
        if (vnfPlatformUuid != null && !vnfPlatformUuid.equals("")) {
            VnfPlatformVO vo = _vnfPlatformDao.findByUuid(vnfPlatformUuid);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find VNF Platform Driver with ID: " + vnfPlatformUuid);
            }
            vnfPlatforms.add(vo);
        } else {
            vnfPlatforms = _vnfPlatformDao.listAll();
            if (vnfPlatforms == null) {
                throw new CloudRuntimeException("Unable to list VNF Platform Drivers");
            }
        }
        if (!vnfPlatforms.isEmpty()) {
            for (VnfPlatformVO vnfPlatformVO : vnfPlatforms) {
                VnfPlatformResponse response = new VnfPlatformResponse(vnfPlatformVO.getUuid(),
                        vnfPlatformVO.getVnfPlatformName(), vnfPlatformVO.getDescription(),
                        vnfPlatformVO.getDriverName(), vnfPlatformVO.getDefaultNic(), vnfPlatformVO.getCreated());
                response.setObjectName("vnfplatformdriver");
                responses.add(response);
            }
        }
        return responses;
    }
}