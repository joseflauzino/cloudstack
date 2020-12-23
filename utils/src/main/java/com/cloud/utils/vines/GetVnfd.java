package com.cloud.utils.vines;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import com.google.gson.Gson;

public class GetVnfd {
private Long serviceofferingid;
private Long templateid;
private String name;
private String displayname;
private List<Long> networkids;/*
private Map<String,String> iptonetworklist;*/
//private Boolean displayvm;


private SimpleVnfd readVnfdFile(String fileName) {
SimpleVnfd vnfd = null;
Gson gson = new Gson();
BufferedReader data;
try {
data = new BufferedReader(new FileReader(fileName));
vnfd = gson.fromJson(data, SimpleVnfd.class);
} catch (FileNotFoundException e) {
e.printStackTrace();
}
return vnfd;
}

/////////////////////////////////////////////////////
////////////////// Constructor //////////////////////
/////////////////////////////////////////////////////
public GetVnfd(String vnfpId) {
SimpleVnfd vnfd = readVnfdFile("/var/cloudstack-vnfm/vnfp_repository/"+vnfpId+"/Descriptors/vnfd.json");
/*String file_name;
file_name = vnfdId+".ser";
try {
FileInputStream fis = new FileInputStream("/var/cloudstack-vnfm/vnfd/"+file_name);
ObjectInputStream ois = new ObjectInputStream(fis);
vnfd = (SimpleVnfd) ois.readObject();*/
this.serviceofferingid = vnfd.getServiceOfferingId();
this.templateid = vnfd.getTempateId();
this.name = vnfd.getName();
this.displayname = vnfd.getDisplayName();
this.networkids = vnfd.getNetworkIds();/*
this.iptonetworklist = vnfd.getIpToNetworkList();*/
//this.displayvm = vnfd.getDisplayVm();
/*} catch (FileNotFoundException e1) {
e1.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
} catch (ClassNotFoundException e) {
e.printStackTrace();
}*/
}
/////////////////////////////////////////////////////
/////////////////// Accessors ///////////////////////
/////////////////////////////////////////////////////
public Long getServiceOfferingId() {
return this.serviceofferingid;
}
public Long getTemplateId() {
return this.templateid;
}
public String getName() {
return this.name;
}
public String getDisplayName() {
return this.displayname;
}
public List<Long> getNetworkIds() {
return this.networkids;
}/*
public Map<String,String> getIpToNetworkList() {
return this.iptonetworklist;
}*/
public boolean isDisplayVm() {
return true;
}
}