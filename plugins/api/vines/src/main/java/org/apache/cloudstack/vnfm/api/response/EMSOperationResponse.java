package org.apache.cloudstack.vnfm.api.response;

import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class EMSOperationResponse extends BaseResponse {

    @SerializedName("type")
    @Param(description = "the EMS operation type")
    private String type;

    @SerializedName("success")
    @Param(description = "true if successfully executed, false otherwise")
    private boolean success;

    @SerializedName("data")
    @Param(description = "the response data")
    private String data;

    /**
     * Represents a EMS Operation response
     *
     * @param type    the EMS operation type
     * @param success true if successfully executed, false otherwise
     * @param data    the response data
     */
    public EMSOperationResponse(String type, boolean success, String data) {
        this.type = type;
        this.success = success;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getData() {
        return data;
    }
}