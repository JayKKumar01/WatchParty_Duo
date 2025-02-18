package com.github.jaykkumar01.watchparty_duo.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class YouTubePlaybackState implements Serializable {
    private final boolean isPlaying;
    private final int currentPosition;

    public YouTubePlaybackState(boolean isPlaying, int currentPosition) {
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @NonNull
    @Override
    public String toString() {
        return "YouTubePlaybackState{" +
                "isPlaying=" + isPlaying +
                ", currentPosition=" + currentPosition +
                '}';
    }
}
