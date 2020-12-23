package com.cloud.utils.vines;

import java.io.Serializable;

public class Vnf implements Serializable{
private String vnfpId;
private Long vmId;

public Vnf(String vnfpId, Long vmId) {
this.vnfpId = vnfpId;
this.vmId = vmId;
}

    /////////////////////////////////////////////////////
    ////////////////// GET METHODS //////////////////////
    /////////////////////////////////////////////////////

public String getVnfpId() {
return vnfpId;
}
public Long getVmId() {
return vmId;
}
}