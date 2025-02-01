package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class Peer implements Serializable {
    private String name;
    private String peerId;
    private String remoteId;

    public Peer(String name){
        this.name = name;
    }
    public Peer(String name, String peerId, String remoteId){
        this.name = name;
        this.peerId = peerId;
        this.remoteId = remoteId;
    }

    public String getName() {
        return name;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
