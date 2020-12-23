package org.apache.cloudstack.vnfm.api.response;

import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class VnfStateNotificationResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "if VNF is up return true, otherwise false")
    private boolean vnfIsUp;

    public VnfStateNotificationResponse(boolean vnfIsUp) {
        this.vnfIsUp = vnfIsUp;
    }

    public boolean getVnfIsUp() {
        return vnfIsUp;
    }
}