package org.apache.cloudstack.util;

import com.cloud.network.dao.NetworkVO;
import com.cloud.utils.vines.ToscaVnffgd;
import com.cloud.vm.VMInstanceVO;

public class CreateSfcPartialResponse {
    private boolean success;
    private NetworkVO trafficNetwork;
    private ToscaVnffgd vnffgd;
    private VMInstanceVO lastVnfVm;
    private String lastVnfIP;

    public CreateSfcPartialResponse(boolean success, NetworkVO trafficNetwork, ToscaVnffgd vnffgd,
            VMInstanceVO lastVnfVm, String lastVnfIP) {
        this.success = success;
        this.trafficNetwork = trafficNetwork;
        this.vnffgd = vnffgd;
        this.lastVnfVm = lastVnfVm;
        this.lastVnfIP = lastVnfIP;
    }

    public boolean isSuccess() {
        return success;
    }

    public NetworkVO getTrafficNetwork() {
        return trafficNetwork;
    }

    public ToscaVnffgd getVnffgd() {
        return vnffgd;
    }

    public VMInstanceVO getLastVnfVm() {
        return lastVnfVm;
    }

    public String getLastVnfIP() {
        return lastVnfIP;
    }

}
