package com.cloud.utils.vines;

import java.io.Serializable;

public class Path implements Serializable{

String vnfId;
String connectionPoint;

/////////////////////////////////////////////////////
////////////////// SET METHODS //////////////////////
/////////////////////////////////////////////////////

public void setVnfId(String vnfId) {
this.vnfId = vnfId;
}
public void setConnectionPoint(String connectionPoint) {
this.connectionPoint = connectionPoint;
}

/////////////////////////////////////////////////////
////////////////// GET METHODS //////////////////////
/////////////////////////////////////////////////////

public String getVnfId() {
return vnfId;
}
public String getConnectionPoint() {
return connectionPoint;
}
}