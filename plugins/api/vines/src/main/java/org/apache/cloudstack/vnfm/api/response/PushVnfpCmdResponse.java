package org.apache.cloudstack.vnfm.api.response;

import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class PushVnfpCmdResponse extends BaseResponse {
    @SerializedName("result")
    @Param(description = "VNFP push result")
    private String result;

    public void setPushVnfpResponse(boolean success) {
        if (success) {
            this.result = "success";
        } else {
            this.result = "error";
        }
    }
}