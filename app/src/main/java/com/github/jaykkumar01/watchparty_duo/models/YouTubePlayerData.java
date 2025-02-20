package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class YouTubePlayerData implements Serializable {
    private final String lastVideoId;
    private final String videoTitle;
    private final int duration;

    public YouTubePlayerData(String lastVideoId, String videoTitle, int duration) {
        this.lastVideoId = lastVideoId;
        this.videoTitle = videoTitle;
        this.duration = duration;
    }

    public String getLastVideoId() {
        return lastVideoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public int getDuration() {
        return duration;
    }

}
