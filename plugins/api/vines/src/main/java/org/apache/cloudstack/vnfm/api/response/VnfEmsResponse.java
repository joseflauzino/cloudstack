package org.apache.cloudstack.vnfm.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class VnfEmsResponse {
    @SerializedName("id")
    @Param(description = "the VNF ID")
    private String id;

    @SerializedName("ip")
    @Param(description = "the VNF IP")
    private String ip;

    @SerializedName("vnf_exp")
    @Param(description = "the VNF Execution Platform")
    private String vnfExp;

    public VnfEmsResponse(String id, String ip, String vnfExp) {
        this.id = id;
        this.ip = ip;
        this.vnfExp = vnfExp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVnfExp() {
        return vnfExp;
    }

    public void setVnfExp(String vnfExp) {
        this.vnfExp = vnfExp;
    }
}
