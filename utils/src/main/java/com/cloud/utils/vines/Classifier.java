package com.cloud.utils.vines;

import java.io.Serializable;

public class Classifier implements Serializable{

String protocol;
String port;

/////////////////////////////////////////////////////
////////////////// SET METHODS //////////////////////
/////////////////////////////////////////////////////

public void setProtocol(String protocol) {
this.protocol = protocol;
}
public void setPort(String port) {
this.port = port;
}

/////////////////////////////////////////////////////
////////////////// GET METHODS //////////////////////
/////////////////////////////////////////////////////

public String getProtocol() {
return protocol;
}
public String getPort() {
return port;
}
}