package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.playeractivityhelpers.MediaHandler;
import com.google.gson.Gson;

public class YouTubePlayerHandler {
    private final Activity activity;
    private final YouTubePlayer player;
    private final YouTubePlayerManager playerManager; // ✅ New instance
    private final TextView currentYouTubeTxt;
    private final WebView webView;

    private String lastVideoId;
    private long lastPosition = 0;
    private boolean isPaused = false;
    private boolean isClosed = true;
    private boolean isPlayerReady = false;

    public YouTubePlayerHandler(Activity activity) {
        this.activity = activity;
        this.webView = activity.findViewById(R.id.webViewYouTube);
        this.playerManager = new YouTubePlayerManager(this); // ✅ Initialize the manager
        this.player = new YouTubePlayer(this, activity, playerManager,webView); // ✅ Pass it to YouTubePlayer
        currentYouTubeTxt = activity.findViewById(R.id.currentYouTubeTxt);
    }

    public void onReady() {
        isPlayerReady = true;
    }

    Handler handler = new Handler();
    public void onPlayerReady(String jsonVideoTitle){
        // Convert JSON string back to a normal String
        String videoTitle = new Gson().fromJson(jsonVideoTitle, String.class);

        activity.runOnUiThread(() -> {
            currentYouTubeTxt.setText(videoTitle); // send this data to remote also
            currentYouTubeTxt.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "Video Title: " + videoTitle, Toast.LENGTH_LONG).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    webView.setVisibility(View.VISIBLE);
                }
            },1000);
        });
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
