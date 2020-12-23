package org.apache.cloudstack.nfvo;

import java.util.List;

import org.apache.cloudstack.nfvo.api.command.CreateSfcCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnfPlatformCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnffgdCmd;
import org.apache.cloudstack.nfvo.api.command.ListSfcsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnfPlatformsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnffgdsCmd;
import org.apache.cloudstack.nfvo.api.response.SfcResponse;
import org.apache.cloudstack.nfvo.api.response.VnfPlatformResponse;
import org.apache.cloudstack.nfvo.api.response.VnffgdResponse;
import org.apache.cloudstack.nfvo.vo.SfcVO;
import org.apache.cloudstack.nfvo.vo.VnfPlatformVO;
import org.apache.cloudstack.nfvo.vo.VnffgdVO;

import com.cloud.network.dao.NetworkVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.user.Account;
import com.cloud.utils.vines.ToscaVnffgd;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vnfm.VnfVO;

/**
 * Vines' NFV Ochestrator
 *
 * @author Jose Flauzino
 */
public interface NFVOrchestrator {

    /**
     * Creates a Service Function Chain (SFC)
     *
     * @param cmd the CreateSfcCmd
     * @return the SfcResponse
     */
    public SfcResponse createSfc(CreateSfcCmd cmd);

    /**
     * Creates a VNF Platform
     *
     * @param cmd the CreateVnfPlatformCmd
     * @return the VnfPlatformDriverResponse
     */
    public VnfPlatformResponse createVnfPlatform(CreateVnfPlatformCmd cmd);

    /**
     * List one or all SFCs
     *
     * @param cmd the ListSfcsCmd
     * @return the SfcResponse list
     */
    public List<SfcResponse> listSfc(ListSfcsCmd cmd);

    /**
     * List one or all VNF Platform Drivers
     *
     * @param cmd the ListVnfPlatformDriversCmd
     * @return the VnfPlatformDriverResponse list
     */
    public List<VnfPlatformResponse> listVnfPlatform(ListVnfPlatformsCmd cmd);

    /**
     * Creates the firewall rules to enable the flow
     *
     * @param trafficNetwork the guest network VO that will be used to forward the
     *                       traffic
     * @param vnffgd         the VNF Forward Graph Descriptor
     * @param lastVnfVm      the Virtual Machine VO that associated with the last
     *                       VNF of the SFC
     * @return the firewall rule created
     */
    // TODO: check if all firewall rules are being applied, because only one is
    // being returned
    FirewallRule createSfcFirewallRules(NetworkVO trafficNetwork, ToscaVnffgd vnffgd, VMInstanceVO lastVnfVm,
            String lastVnfIP);

    /**
     * Saves the SFC info to the DB
     *
     * @param vnffgdUuid the VNF Forward Graph Descriptor UUID
     * @return the SFC VO saved
     */
    public SfcVO createSfcRecord(String vnffgdUuid);

    /**
     * Saves the VNF Platform info to the DB
     *
     * @param vnfPlatformName the VNF Platform name
     * @param description     a short description
     * @param driverName      the driver name
     * @param defaultNic      the default NIC
     * @return the VNF Platform VO saved
     */
    public VnfPlatformVO createVnfPlatformRecord(String vnfPlatformName, String description, String driverName,
            String defaultNic);

    /**
     * Creates a VNF Forward Graph Descriptor (VNFFGD)
     *
     * @param cmd the CreateVnffgdCmd
     * @return the VnffgdResponse
     */
    public VnffgdResponse createVnffgd(CreateVnffgdCmd cmd);

    /**
     * Saves the VNFFGD info to the DB
     *
     * @param url the URL to where the VNFGD is hosted
     * @return the VNFFGD VO saved
     */
    public VnffgdVO createVnffgdRecord(String url);

    /**
     * List one or all VNFFGDs
     *
     * @param cmd the ListVnffgdsCmd
     * @return the VnffgdResponse list
     */
    public List<VnffgdResponse> listVnffgd(ListVnffgdsCmd cmd);

    /**
     * Apply the SFC firewall rules
     *
     * @param rule   the firewall rule to be applied
     * @param caller the current client/user
     */
    // TODO: maybe change that for a rule list
    public void applySfcFirewallRules(FirewallRule rule, Account caller);

    // Util (talvez passar algumas dessas funcoes pro VNFM ou criar um utilitario
    // que o NFVO e o VNFM usam)
    public String findGuestNetworkGatewayIp(String networkUuid);

    public String findRouterIP(String guestGatewayIP);

    public String getVnfMgmtInterfaceUuid(VnfVO vnfVO);

    public String getVnfVmIp(long vnfVmId, String guestNetworkGateway);

    public VMInstanceVO findVnfVmInstance(String vnfUuid); // talvez passar isso pro VNFM
}