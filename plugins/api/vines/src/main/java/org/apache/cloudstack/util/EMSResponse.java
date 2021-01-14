package org.apache.cloudstack.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class EMSResponse implements Serializable {
    @SerializedName("status")
    @Param(description = "the response status. success or error")
    private String status;

    @SerializedName("message")
    @Param(description = "the returned message")
    private String message;

    @SerializedName("object")
    @Param(description = "the object list")
    private List<Map<String, String>> object;

    public EMSResponse(String status, String message, List<Map<String, String>> object) {
        this.status = status;
        this.message = message;
        this.object = object;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, String>> getObject() {
        return object;
    }

    public void setObject(List<Map<String, String>> object) {
        this.object = object;
    }
}