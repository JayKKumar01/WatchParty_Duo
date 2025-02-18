package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.util.Log;

public class YouTubePlayerManager {
    private final YouTubePlayerHandler handler;
    private int lastEvent = -1;

    public YouTubePlayerManager(YouTubePlayerHandler handler) {
        this.handler = handler;
    }

    public void onPlay(long timeMs) {
        if (lastEvent == 1) return; // Skip duplicate events
        lastEvent = 1;
        Log.d("YouTubePlayerManager", "Playing at " + timeMs + " ms.");
    }

    public void onPause(long timeMs) {
        if (lastEvent == 2) return; // Skip duplicate events
        lastEvent = 2;
        Log.d("YouTubePlayerManager", "Paused at " + timeMs + " ms.");
    }

    public void onSeek(long timeMs) {
        if (lastEvent == 3) return; // Skip duplicate events
        lastEvent = 3;
        Log.d("YouTubePlayerManager", "Seeked to " + timeMs + " ms.");
    }

    public void onFullscreenEntered() {
        Log.d("YouTubePlayerManager", "Fullscreen entered.");
    }

    public void onFullscreenExited() {
        Log.d("YouTubePlayerManager", "Fullscreen exited.");
    }

    public boolean isPlaying() {
        return lastEvent == 1;
    }
}

