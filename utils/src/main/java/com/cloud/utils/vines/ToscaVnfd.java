package com.cloud.utils.vines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cloud.utils.vines.VnfdContent.ConnectionPoint;
import com.cloud.utils.vines.VnfdContent.MgmtInterface;
import com.cloud.utils.vines.VnfdContent.Vdu;
import com.cloud.utils.vines.VnfdContent.VirtualLink;

public class ToscaVnfd implements Serializable {
    private VnfdContent vnfd;

    /////////////////////////////////////////////////////
    /////////////// PUBLICS ACCESSORS ///////////////////
    /////////////////////////////////////////////////////

    public String getVnfName() {
        return vnfd.getShortName();
    }

    public String getVnfPlatformId() {
        return vnfd.getVnfPlatformId();
    }

    public String getServiceOfferingUuid() {
        return vnfd.getVdu().getServiceOfferingId();
    }

    public List<String> getNetworkUuids() {
        return vnfd.getNetworkIds();
    }

    public String getTemplateUuid() {
        return vnfd.getVdu().getTemplateId();
    }

    public String getManagementInterfaceUuid() {

        MgmtInterface mgmtInterface = vnfd.getMgmtInterface();

        // CP reference
        String cp = mgmtInterface.getConnectionPoint();

        String vlName = "";
        for (ConnectionPoint c : vnfd.getConnectionPoint()) {
            if (c.getName().equals(cp)) {
                vlName = c.getVirtuaLink();
            }
        }
        String mgmtUuid = "";
        for (VirtualLink v : vnfd.getVirtualLink()) {
            if (v.getName().equals(vlName)) {
                mgmtUuid = v.getNetworkId();
            }
        }
        return mgmtUuid;
    }

    public String getApp() {
        return vnfd.getApp();
    }
}

// Internal content
class VnfdContent implements Serializable {
    /*
     * name shortName version description vdu image serviceOffering virtualLink[]
     * name networkId connectionPoint[] name virtualLink mgmtInterface
     * connectionPoint lifecycle[] install start stop app
     */
    private String name;
    private String shortName;
    private String vnfPlatformId;
    private String version;
    private String description;
    private Vdu vdu;
    private List<VirtualLink> virtualLink;
    private List<ConnectionPoint> connectionPoint;
    private MgmtInterface mgmtInterface;
    private List<Lifecycle> lifecycle;
    private String app;

    /////////////////////////////////////////////////////
    ////////////////// SET METHODS //////////////////////
    /////////////////////////////////////////////////////

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVdu(Vdu vdu) {
        this.vdu = vdu;
    }

    public void setVirtualLink(List<VirtualLink> virtualLink) {
        this.virtualLink = virtualLink;
    }

    public void setConnectionPoint(List<ConnectionPoint> connectionPoint) {
        this.connectionPoint = connectionPoint;
    }

    public void setMgmtInterface(MgmtInterface mgmtInterface) {
        this.mgmtInterface = mgmtInterface;
    }

    public void setLifecycle(List<Lifecycle> lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void setApp(String app) {
        this.app = app;
    }

    /////////////////////////////////////////////////////
    ////////////////// GET METHODS //////////////////////
    /////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getVnfPlatformId() {
        return vnfPlatformId;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Vdu getVdu() {
        return vdu;
    }

    public List<VirtualLink> getVirtualLink() {
        return virtualLink;
    }

    public List<String> getNetworkIds() {
        List<String> networkIds = new ArrayList<String>();
        for (VirtualLink vl : virtualLink) {
            networkIds.add(vl.getNetworkId());
        }
        return networkIds;
    }

    public List<ConnectionPoint> getConnectionPoint() {
        return connectionPoint;
    }

    public MgmtInterface getMgmtInterface() {
        return mgmtInterface;
    }

    public List<Lifecycle> getLifecycle() {
        return lifecycle;
    }

    public String getApp() {
        return app;
    }

    /////////////////////////////////////////////////////
    ////////////////// SUB CLASSES //////////////////////
    /////////////////////////////////////////////////////
    class Vdu implements Serializable {
        String templateId;
        String serviceOfferingId;

        protected String getTemplateId() {
            return templateId;
        }

        protected String getServiceOfferingId() {
            return serviceOfferingId;
        }
    }

    class VirtualLink implements Serializable {
        String name;
        String networkId;

        protected String getName() {
            return name;
        }

        protected String getNetworkId() {
            return networkId;
        }
    }

    class ConnectionPoint implements Serializable {
        String name;
        String virtualLink;

        protected String getName() {
            return name;
        }

        protected String getVirtuaLink() {
            return virtualLink;
        }
    }

    class Lifecycle implements Serializable {
        String operation;
        String file;
    }

    class MgmtInterface implements Serializable {
        private String connectionPoint;

        String getConnectionPoint() {
            return this.connectionPoint;
        }
    }
}