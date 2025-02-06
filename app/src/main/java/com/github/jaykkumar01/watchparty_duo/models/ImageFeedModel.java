package com.github.jaykkumar01.watchparty_duo.models;

import android.util.Base64;

import java.io.Serializable;
import java.util.List;

public class ImageFeedModel implements Serializable {
    private String base64ImageBytes;
    private final long timestamp;
    private List<String> base64Chunks;

    public ImageFeedModel(String base64ImageBytes, long timestamp) {
        this.base64ImageBytes = base64ImageBytes;
        this.timestamp = timestamp;
    }

    public ImageFeedModel(List<String> base64Chunks, long timestamp) {
        this.timestamp = timestamp;
        this.base64Chunks = base64Chunks;
    }

    public byte[] getBytes() {
        return Base64.decode(base64ImageBytes,Base64.NO_WRAP);
    }

    public long getTimestamp() {
        return timestamp;
    }


}
