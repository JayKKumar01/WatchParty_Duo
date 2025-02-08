package com.github.jaykkumar01.watchparty_duo.models;

import android.util.Base64;

import java.io.Serializable;

public class FeedModel implements Serializable {
    private String base64ImageBytes;
    private final long timestamp;
    private byte[] bytes;
    private int feedType;

    public FeedModel(String base64ImageBytes, long timestamp) {
        this.base64ImageBytes = base64ImageBytes;
        this.timestamp = timestamp;
    }

    public FeedModel(byte[] bytes, long timestamp) {
        this.timestamp = timestamp;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public byte[] getBase64Bytes() {
        return Base64.decode(base64ImageBytes,Base64.NO_WRAP);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getFeedType() {
        return feedType;
    }

    public void setFeedType(int feedType) {
        this.feedType = feedType;
    }
}
