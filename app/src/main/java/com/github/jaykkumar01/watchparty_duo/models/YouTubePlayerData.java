package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class YouTubePlayerData implements Serializable {
    private final String lastVideoId;
    private final String videoTitle;

    public YouTubePlayerData(String lastVideoId, String videoTitle) {
        this.lastVideoId = lastVideoId;
        this.videoTitle = videoTitle;
    }

    public String getLastVideoId() {
        return lastVideoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }
}
