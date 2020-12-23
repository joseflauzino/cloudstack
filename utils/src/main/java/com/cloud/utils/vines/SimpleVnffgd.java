package com.cloud.utils.vines;

import java.io.Serializable;
import java.util.List;

public class SimpleVnffgd implements Serializable{

private String id;
private String url;
private NetworkInfo network;
private List<String> path;
private List<Classifier> classifier;

/////////////////////////////////////////////////////
////////////////// SET METHODS //////////////////////
/////////////////////////////////////////////////////

public void setId(String id) {
this.id = id;
}
public void setUrl(String url) {
this.url = url;
}
public void setNetworkInfo(NetworkInfo network) {
this.network = network;
}
public void setPath(List<String> path) {
this.path = path;
}
public void setClassifier(List<Classifier> classifier) {
this.classifier = classifier;
}

/////////////////////////////////////////////////////
////////////////// GET METHODS //////////////////////
/////////////////////////////////////////////////////

public String getId() {
return id;
}
public String getUrl() {
return url;
}
public String getNet() {
return network.net;
}
public String getNetmask() {
return network.netmask;
}
public List<String> getPath() {
return path;
}
public List<Classifier> getClassifier() {
return classifier;
}

}

class NetworkInfo implements Serializable{
String net;
String netmask;
}