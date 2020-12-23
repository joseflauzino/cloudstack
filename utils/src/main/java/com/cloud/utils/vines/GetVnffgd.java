package com.cloud.utils.vines;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class GetVnffgd {
private String id;
private String url;
private String net;
private String netmask;
private List<String> path;
private List<Classifier> classifier;

private SimpleVnffgd readvnfFile(String fileName, Boolean extension) {
SimpleVnffgd vnffgd = null;
if(!extension) {
fileName = fileName+".ser";
}
try {
FileInputStream fis = new FileInputStream("/var/cloudstack-nfvo/vnffgd/"+fileName);
ObjectInputStream ois = new ObjectInputStream(fis);
vnffgd = (SimpleVnffgd) ois.readObject();
} catch (FileNotFoundException e1) {
e1.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
} catch (ClassNotFoundException e) {
e.printStackTrace();
}
return vnffgd;
}

/////////////////////////////////////////////////////
////////////////// Constructor //////////////////////
/////////////////////////////////////////////////////
public GetVnffgd(String vnffgdId) {
SimpleVnffgd vnffgd = readvnfFile(vnffgdId,false);

this.id = vnffgd.getId();
this.url = vnffgd.getUrl();
this.net = vnffgd.getNet();
this.netmask = vnffgd.getNetmask();
this.path = vnffgd.getPath();
this.classifier = vnffgd.getClassifier();
}
/////////////////////////////////////////////////////
/////////////////// Accessors ///////////////////////
/////////////////////////////////////////////////////
public String getId() {
return this.id;
}
public String getUrl() {
return this.url;
}
public String getNet() {
return this.net;
}
public String getNetmask() {
return this.netmask;
}
public List<String> getPath() {
return this.path;
}
public List<Classifier> getClassifier() {
return this.classifier;
}
}