package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class BaseFeedModel implements Serializable {
    private byte[] rawData;
    private String base64Data;

    public BaseFeedModel(byte[] rawData, String base64Data) {
        this.rawData = rawData;
        this.base64Data = base64Data;
    }
    public BaseFeedModel(byte[] rawData){
        this.rawData = rawData;
    }

    public BaseFeedModel(String base64Data) {
        this.base64Data = base64Data;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }
}
