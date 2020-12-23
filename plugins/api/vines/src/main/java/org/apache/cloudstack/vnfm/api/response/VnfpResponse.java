package org.apache.cloudstack.vnfm.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class VnfpResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the VNF Package ID")
    private String vnfpId;

    @SerializedName("name")
    @Param(description = "the VNF Package name")
    private String name;

    @SerializedName("url")
    @Param(description = "the URL where the VNF Package was downloaded from")
    private String vnfpUrl;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this VNF Package was created")
    private Date created;

    public VnfpResponse(String vnfpId, String name, String vnfpUrl, Date created) {
        this.vnfpId = vnfpId;
        this.name = name;
        this.vnfpUrl = vnfpUrl;
        this.created = created;
    }

    public String getVnfpId() {
        return vnfpId;
    }

    public String getName() {
        return name;
    }

    public String getVnfpUrl() {
        return vnfpUrl;
    }

    public Date getCreated() {
        return created;
    }
}