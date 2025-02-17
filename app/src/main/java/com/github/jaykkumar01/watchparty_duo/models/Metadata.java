package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class Metadata implements Serializable {
    private final int latency;
    private final int resolution;
    private final int fps;

    public Metadata(int latency, int resolution, int fps) {
        this.latency = latency;
        this.resolution = resolution;
        this.fps = fps;
    }

    public int getLatency() {
        return latency;
    }

    public int getResolution() {
        return resolution;
    }

    public int getFps() {
        return fps;
    }

    @Override
    public String toString() {
        return "FeedMetadata{" +
                "latency=" + latency +
                ", resolution=" + resolution +
                ", fps=" + fps +
                '}';
    }
}

