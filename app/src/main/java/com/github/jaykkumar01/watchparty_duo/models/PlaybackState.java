package com.github.jaykkumar01.watchparty_duo.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class PlaybackState implements Serializable {
    private final boolean isPlaying;
    private final long position;

    public PlaybackState(boolean isPlaying, long position) {
        this.isPlaying = isPlaying;
        this.position = position;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public long getPosition() {
        return position;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlaybackState{isPlaying=" + isPlaying + ", position=" + position + "}";
    }
}

