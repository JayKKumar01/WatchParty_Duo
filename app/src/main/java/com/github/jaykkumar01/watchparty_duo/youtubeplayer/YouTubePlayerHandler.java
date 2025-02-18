package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.helpers.YouTubeIDExtractor;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.Objects;

public class YouTubePlayerHandler {
    private final Activity activity;
    private final YouTubePlayer player;
    private final YouTubePlayerManager playerManager; // ✅ New instance
    private TextView currentYouTubeTxt;
    private AppCompatButton createYouTubePlayer,playYouTubePlayer;
    private final WebView webView;

    private String lastVideoId;
    private long lastPosition = 0;
    private boolean isPaused = false;
    private boolean isClosed = true;
    private boolean isYouTubeIFrameAPIReady = false;
    private ConstraintLayout layoutCreateYouTubePlayer,layoutPlayYouTubePlayer;

    public YouTubePlayerHandler(Activity activity) {
        this.activity = activity;
        this.webView = activity.findViewById(R.id.webViewYouTube);
        this.playerManager = new YouTubePlayerManager(this); // ✅ Initialize the manager
        this.player = new YouTubePlayer(this, activity, playerManager,webView); // ✅ Pass it to YouTubePlayer

        initializeUI();
    }

    private void initializeUI() {
        // ✅ Add new YouTube components
        currentYouTubeTxt = activity.findViewById(R.id.currentYouTubeTxt);
        TextInputEditText etYouTubeLink = activity.findViewById(R.id.etYouTubeLink);
        createYouTubePlayer = activity.findViewById(R.id.createYouTubePlayer);
        playYouTubePlayer = activity.findViewById(R.id.playYouTubePlayer);
        layoutCreateYouTubePlayer = activity.findViewById(R.id.layoutCreateYouTubePlayer);
        layoutPlayYouTubePlayer = activity.findViewById(R.id.layoutPlayYouTubePlayer);

        // Pass etYouTubeLink directly to handle YouTubeClick
        createYouTubePlayer.setOnClickListener(view -> handleCreateYouTubePlayerClick(etYouTubeLink));
    }

    private void handleCreateYouTubePlayerClick(TextInputEditText etYouTubeLink) {
        String link = Objects.requireNonNull(etYouTubeLink.getText()).toString().trim();

        if (link.isEmpty()) {
            Toast.makeText(activity, "Please enter a YouTube link", Toast.LENGTH_SHORT).show();
            return;
        }

        String videoId = YouTubeIDExtractor.extractYouTubeVideoId(link);
        if (videoId == null) {
            Toast.makeText(activity, "Invalid YouTube link", Toast.LENGTH_SHORT).show();
            return;
        }
        etYouTubeLink.setText("");
        Base.hideKeyboard(activity);
        createYouTubePlayer.setEnabled(false);
        createYouTubePlayer.setText("Creating...");
        fetchVideoTitle(videoId);
    }

    private void fetchVideoTitle(String videoId) {
        if (!isYouTubeIFrameAPIReady) return;
        player.fetchVideoTitle(videoId);
    }

    public void onReady() {
        isYouTubeIFrameAPIReady = true;
    }

    Handler handler = new Handler();
    public void onPlayerReady(String jsonVideoTitle){
        // Convert JSON string back to a normal String
        String videoTitle = new Gson().fromJson(jsonVideoTitle, String.class);

        activity.runOnUiThread(() -> {
            currentYouTubeTxt.setText(videoTitle); // send this data to remote also
            layoutCreateYouTubePlayer.setVisibility(View.GONE);
            layoutPlayYouTubePlayer.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //webView.setVisibility(View.VISIBLE);
                }
            },1000);
        });
    }

    public void playVideo(String videoId) {
        if (!isYouTubeIFrameAPIReady) return;
        isClosed = false;

        lastVideoId = videoId;

        player.loadVideo(videoId, isPaused ? 0: 1);
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
