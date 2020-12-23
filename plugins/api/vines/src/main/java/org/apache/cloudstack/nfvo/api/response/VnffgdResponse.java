package org.apache.cloudstack.nfvo.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class VnffgdResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the VNF Forward Graph Descriptor ID")
    private String vnffgdId;

    @SerializedName("url")
    @Param(description = "the URL where the VNFFGD was downloaded from")
    private String url;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this SFC was created")
    private Date created;

    public VnffgdResponse(String vnffgdId, String url, Date created) {
        this.vnffgdId = vnffgdId;
        this.url = url;
        this.created = created;
    }

    public String getVnffgdId() {
        return vnffgdId;
    }

    public String getUrl() {
        return url;
    }

    public Date getCreated() {
        return created;
    }
}