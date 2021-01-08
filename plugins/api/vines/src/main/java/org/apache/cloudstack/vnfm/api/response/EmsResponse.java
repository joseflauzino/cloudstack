package org.apache.cloudstack.vnfm.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class EmsResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the EMS ID")
    private String emsId;

    @SerializedName("name")
    @Param(description = "the EMS name")
    private String name;

    @SerializedName("url")
    @Param(description = "the EMS IP address")
    private String emsIp;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this EMS was created")
    private Date created;

    public EmsResponse(String emsId, String name, String emsIp, Date created) {
        this.emsId = emsId;
        this.name = name;
        this.emsIp = emsIp;
        this.created = created;
    }

    public String getEmsId() {
        return emsId;
    }

    public String getName() {
        return name;
    }

    public String getVnfpUrl() {
        return emsIp;
    }

    public Date getCreated() {
        return created;
    }
}