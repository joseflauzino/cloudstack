package org.apache.cloudstack.vnfm.api.response;

import java.util.List;
import java.util.Map;

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

    @SerializedName("message")
    @Param(description = "the response message")
    private String message;

    @SerializedName("object")
    @Param(description = "the object list")
    private List<Map<String, String>> object;

    /**
     * Represents a EMS Operation response
     *
     * @param type    the EMS operation type
     * @param success true if successfully executed, false otherwise
     * @param message the response message
     * @param object  the response object list. Valid object types are: ems, vnf,
     *                subscription, and sfc.
     */
    public EMSOperationResponse(String type, boolean success, String message, List<Map<String, String>> object) {
        this.type = type;
        this.success = success;
        this.message = message;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Map<String, String>> getObject() {
        return object;
    }
}