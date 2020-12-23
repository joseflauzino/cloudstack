package org.apache.cloudstack.nfvo.api.response;

import java.util.Date;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;;

public class VnfPlatformResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the VNF Platform Driver ID")
    private String vnfPlatformDriverId;

    @SerializedName("vnfplatformname")
    @Param(description = "The VNF Platform name")
    private String vnfPlatformName;

    @SerializedName("description")
    @Param(description = "A short description")
    private String description;

    @SerializedName("drivername")
    @Param(description = "The VNF Platform Driver name")
    private String driverName;

    @SerializedName("defaultnic")
    @Param(description = "The default VNF NIC")
    private String defaultNic;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this VNF Platform Driver was created")
    private Date created;

    /**
     * Represents a VNF Platform Driver response
     *
     * @param vnfPlatformDriverId the VNF Platform Driver UUID
     * @param vnfPlatformName     the VNF Platform name
     * @param description         a short description
     * @param driverName          the driver name
     * @param defaultNic          the default NIC
     * @param created             the created date
     */
    public VnfPlatformResponse(String vnfPlatformDriverId, String vnfPlatformName, String description,
            String driverName, String defaultNic, Date created) {
        this.vnfPlatformDriverId = vnfPlatformDriverId;
        this.vnfPlatformName = vnfPlatformName;
        this.description = description;
        this.driverName = driverName;
        this.defaultNic = defaultNic;
        this.created = created;
    }

    public String getVnfPlatformDriverId() {
        return vnfPlatformDriverId;
    }

    public String getVnfPlatformName() {
        return vnfPlatformName;
    }

    public String getDescription() {
        return description;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDefaultNic() {
        return defaultNic;
    }

    public Date getCreated() {
        return created;
    }
}