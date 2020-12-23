package com.cloud.utils.vines;

import java.io.Serializable;
import java.util.List;

public class ToscaVnffgd implements Serializable{
    private VnffgdContent vnffgd;

    public String getNetwork() {
        return vnffgd.getNetwork();
    }

    public List<Classifier> getClassifier() {
        return vnffgd.getClassifier();
    }

    public List<Path> getPath() {
        return vnffgd.getPath();
    }
}

class VnffgdContent implements Serializable{
    /*
     * network
     * path[]
     *     vnfId
     *     connectionPoint
     * classifier[]
     *     protocol
     *     port
     * */
    private String network;
    private List<Path> path;
    private List<Classifier> classifier;

    /////////////////////////////////////////////////////
    ////////////////// SET METHODS //////////////////////
    /////////////////////////////////////////////////////

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setPath(List<Path> path) {
        this.path = path;
    }

    public void setClassifier(List<Classifier> classifier) {
        this.classifier = classifier;
    }

    /////////////////////////////////////////////////////
    ////////////////// GET METHODS //////////////////////
    /////////////////////////////////////////////////////

    public String getNetwork() {
        return network;
    }

    public List<Path> getPath() {
        return path;
    }

    public List<Classifier> getClassifier() {
        return classifier;
    }


    /////////////////////////////////////////////////////
    ////////////////// SUB CLASSES //////////////////////
    /////////////////////////////////////////////////////
}