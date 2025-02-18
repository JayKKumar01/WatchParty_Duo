package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.app.Activity;
import android.webkit.WebView;
import android.widget.Toast;

public class YouTubePlayerHandler {
    private final Activity activity;
    private final YouTubePlayer player;
    private final YouTubePlayerManager playerManager; // ✅ New instance

    private String lastVideoId;
    private long lastPosition = 0;
    private boolean isPaused = false;
    private boolean isClosed = true;
    private boolean isPlayerReady = false;

    public YouTubePlayerHandler(Activity activity) {
        this.activity = activity;
        this.playerManager = new YouTubePlayerManager(this); // ✅ Initialize the manager
        this.player = new YouTubePlayer(this, activity, playerManager); // ✅ Pass it to YouTubePlayer
    }

    public void onPlayerReady() {
        isPlayerReady = true;
        activity.runOnUiThread(() -> Toast.makeText(activity, "Player is Ready", Toast.LENGTH_SHORT).show());

    }

    public void playVideo(String videoId) {
        if (!isPlayerReady) return;
        isClosed = false;

        lastVideoId = videoId;

        player.loadVideo(videoId);
    }

    public void onRestart() {
        if (lastVideoId != null && !isClosed) {
            playVideo(lastVideoId);
        }
    }

    public void onStop() {
        player.stop();
    }

    public void onReset(){
        isClosed = true;
        lastVideoId = null;
    }
}
