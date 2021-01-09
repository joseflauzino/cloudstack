package org.apache.cloudstack.vnfm.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class VnfResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the VNF ID")
    private String vnfId;

    @SerializedName("name")
    @Param(description = "the VNF name")
    private String name;

    @SerializedName("vnfpid")
    @Param(description = "the VNF Package ID")
    private String vnfpId;

    @SerializedName("emsid")
    @Param(description = "the EMS ID")
    private String emsId;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this VNF was deployed")
    private Date created;

    public VnfResponse(String vnfId, String name, String vnfpId, String emsId, Date created) {
        this.vnfId = vnfId;
        this.name = name;
        this.vnfpId = vnfpId;
        this.emsId = emsId;
        this.created = created;
    }

    public String getVnfId() {
        return vnfId;
    }

    public String getName() {
        return name;
    }

    public String getVnfpId() {
        return vnfpId;
    }

    public String getEmsId() {
        return emsId;
    }

    public Date getCreated() {
        return created;
    }
}