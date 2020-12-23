package org.apache.cloudstack.util;

public class Vnf {
    private String id;
    private String name;
    private String vnfpId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVnfpId() {
        return vnfpId;
    }

    public void setVnfpId(String vnfpId) {
        this.vnfpId = vnfpId;
    }
}
