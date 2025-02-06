package com.github.jaykkumar01.watchparty_duo.models;

import android.util.Base64;

import java.io.Serializable;

public class ImageFeedModel implements Serializable {
    private final String base64ImageBytes;
    private final long timestamp;

    public ImageFeedModel(String base64ImageBytes, long timestamp) {
        this.base64ImageBytes = base64ImageBytes;
        this.timestamp = timestamp;
    }

    public byte[] getBytes() {
        return Base64.decode(base64ImageBytes,Base64.NO_WRAP);
    }

    public long getTimestamp() {
        return timestamp;
    }


}
