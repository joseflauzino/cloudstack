package org.apache.cloudstack.nfvo.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class SfcResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the SFC ID")
    private String sfcId;

    @SerializedName("vnffgdid")
    @Param(description = "the associated VNFFGD ID")
    private String vnffgdId;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this SFC was created")
    private Date created;

    /**
     * Represents a SFC response
     *
     * @param sfcId    the SFC UUID
     * @param vnffgdId the VNFFGD UUID
     * @param created  the created date
     */
    public SfcResponse(String sfcId, String vnffgdId, Date created) {
        this.sfcId = sfcId;
        this.vnffgdId = vnffgdId;
        this.created = created;
    }

    public String getSfcId() {
        return sfcId;
    }

    public String getVnffgdId() {
        return vnffgdId;
    }

    public Date getCreated() {
        return created;
    }
}