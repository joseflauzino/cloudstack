package org.apache.cloudstack.util;

import java.io.Serializable;

public class EMSResponse implements Serializable {
    private String status;
    private String data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setDate(String date) {
        this.data = date;
    }
}
