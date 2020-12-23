package com.cloud.utils.vines;

import java.io.Serializable;
import java.util.List;

public class SimpleVnfd implements Serializable{
//private String id;
private Long serviceOfferingId;
private Long templateId;
private String name;
private String displayName;
private List<Long> networkIds;
//private Map<String,String> ipToNetworkList;
//private Boolean displayVm;

    /////////////////////////////////////////////////////
    ////////////////// SET METHODS //////////////////////
    /////////////////////////////////////////////////////

/*public void setId(String id) {
this.id = id;
}*/
public void setServiceOfferingId(Long serviceOfferingId) {
this.serviceOfferingId = serviceOfferingId;
}
public void setTempateId(Long templateId) {
this.templateId = templateId;
}
public void setName(String name) {
this.name = name;
}
public void setdisplayName(String displayName) {
this.displayName = displayName;
}
public void setNetworkIds(List<Long> networkIds) {
this.networkIds = networkIds;
}/*
public void setIpToNetworkList(Map<String,String> ipToNetworkList) {
this.ipToNetworkList = ipToNetworkList;
}*/
/*
public void setDisplayVm(Boolean displayVm) {
this.displayVm = displayVm;
}*/

    /////////////////////////////////////////////////////
    ////////////////// GET METHODS //////////////////////
    /////////////////////////////////////////////////////

/*public String getId() {
return id;
}*/
public Long getServiceOfferingId() {
return serviceOfferingId;
}
public Long getTempateId() {
return templateId;
}
public String getName() {
return name;
}
public Boolean isDisplayVm() {
return true;// static
}
public List<Long> getNetworkIds() {
return networkIds;
}/*
public Map<String,String> getIpToNetworkList() {
return ipToNetworkList;
}*/

public String getDisplayName() {
return displayName;
}
}