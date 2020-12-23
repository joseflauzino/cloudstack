package org.apache.cloudstack.util;

import java.util.ArrayList;
import java.util.List;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.IpAddress;
import com.cloud.network.rules.FirewallRule;
import com.cloud.utils.net.NetUtils;

public class FirewallRuleUtil implements FirewallRule {

    /////////////////////////////////////////////////////
    ////////////////// Parameters ///////////////////////
    /////////////////////////////////////////////////////

    // the IP address id of the port forwarding rule
    private Long ipAddressId;

    // the protocol for the firewall rule. Valid values are TCP/UDP/ICMP.
    private String protocol;

    // the starting port of firewall rule
    private Integer publicStartPort;

    // the ending port of firewall rule
    private Integer publicEndPort;

    // the CIDR list to forward traffic from
    private List<String> cidrlist;

    // type of the ICMP message being sent
    private Integer icmpType;

    // error code for this icmp message
    private Integer icmpCode;

    // type of firewallrule: system/user
    private String type;

    private long accountId;

    private IpAddress ip;

    /////////////////////////////////////////////////////
    ////////////////// Constructor //////////////////////
    /////////////////////////////////////////////////////

    public FirewallRuleUtil(Long ipAddressId, String protocol, Integer publicStartPort, Integer publicEndPort,
            List<String> cidrlist, Integer icmpType, Integer icmpCode, long accountId, IpAddress ip) {
        this.ipAddressId = ipAddressId;
        this.protocol = protocol;
        this.publicStartPort = publicStartPort;
        this.publicEndPort = publicEndPort;
        this.cidrlist = cidrlist;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.accountId = accountId;
        this.ip = ip;
    }

    /////////////////////////////////////////////////////
    //////////////////// Getters ////////////////////////
    /////////////////////////////////////////////////////

    public Long getIpAddressId() {
        return ipAddressId;
    }

    @Override
    public String getProtocol() {
        return protocol.trim();
    }

    @Override
    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            return cidrlist;
        } else {
            List<String> oneCidrList = new ArrayList<String>();
            oneCidrList.add(NetUtils.ALL_IP4_CIDRS);
            return oneCidrList;
        }
    }

    public void setSourceCidrList(List<String> cidrs) {
        cidrlist = cidrs;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    private IpAddress getIp() {
        return ip;
    }

    @Override
    public Integer getIcmpCode() {
        if (icmpCode != null) {
            return icmpCode;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    @Override
    public Integer getIcmpType() {
        if (icmpType != null) {
            return icmpType;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;

        }
        return null;
    }

    @Override
    public Long getRelated() {
        return null;
    }

    @Override
    public FirewallRuleType getType() {
        return FirewallRuleType.User;
    }

    @Override
    public TrafficType getTrafficType() {
        return FirewallRule.TrafficType.Ingress;
    }

    @Override
    public boolean isDisplay() {
        return true;
    }

    @Override
    public List<String> getDestinationCidrList() {
        return null;
    }

    @Override
    public Class<?> getEntityType() {
        return FirewallRule.class;
    }

    @Override
    public long getDomainId() {
        return ip.getDomainId();
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException("database ID can only provided by VO objects");
    }

    @Override
    public String getXid() {
        return null;
    }

    @Override
    public Integer getSourcePortStart() {
        if (publicStartPort != null) {
            return publicStartPort;
        }
        return null;
    }

    @Override
    public Integer getSourcePortEnd() {
        if (publicEndPort == null) {
            if (publicStartPort != null) {
                return publicStartPort;
            }
        } else {
            return publicEndPort;
        }
        return null;
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.Firewall;
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Should never call me to find the state");
    }

    @Override
    public long getNetworkId() {
        Long ntwkId = null;
        if (ip.getAssociatedWithNetworkId() != null) {
            ntwkId = ip.getAssociatedWithNetworkId();
        }
        if (ntwkId == null) {
            throw new InvalidParameterValueException("Unable to create firewall rule for the IP address ID="
                    + ipAddressId + " as IP is not associated with any network and no networkId is passed in");
        }
        return ntwkId;
    }

    @Override
    public Long getSourceIpAddressId() {
        return ipAddressId;
    }
}
