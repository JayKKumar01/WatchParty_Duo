package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.helpers.YouTubeIDExtractor;
import com.github.jaykkumar01.watchparty_duo.managers.YouTubePlayerManager;
import com.github.jaykkumar01.watchparty_duo.models.YouTubePlayerData;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class YouTubePlayerHandler {
    private final Activity activity;
    private final YouTubePlayer player;
    private final YouTubePlayerManager playerManager; // ✅ New instance
    private TextView currentYouTubeTxt;
    private AppCompatButton createYouTubePlayer,playYouTubePlayer;
    TextView recreateYouTubePlayer,replayYouTubePlayer;
    private final WebView webView;

    private String lastVideoId;
    private int lastPosition = 0;
    private boolean isPaused = false;
    private boolean isClosed = true;
    private boolean isYouTubeIFrameAPIReady = false;
    private ConstraintLayout layoutCreateYouTubePlayer,layoutPlayYouTubePlayer;

    public YouTubePlayerHandler(Activity activity) {
        this.activity = activity;
        this.webView = activity.findViewById(R.id.webViewYouTube);
        this.playerManager = new YouTubePlayerManager(activity,this); // ✅ Initialize the manager
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
        recreateYouTubePlayer = activity.findViewById(R.id.recreateYouTubePlayer);
        replayYouTubePlayer = activity.findViewById(R.id.rePlayYouTubePlayer);

        // Pass etYouTubeLink directly to handle YouTubeClick
        createYouTubePlayer.setOnClickListener(view -> handleCreateYouTubePlayerClick(etYouTubeLink));
        recreateYouTubePlayer.setOnClickListener(this::handleRecreateYouTubePlayerClick);
        replayYouTubePlayer.setOnClickListener(this::handleReplayYouTubePlayerClick);
        playYouTubePlayer.setOnClickListener(view -> playVideo());
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
        Base.hideKeyboard(activity);
        createYouTubePlayer.setEnabled(false);
        createYouTubePlayer.setText("Creating...");
        fetchVideoTitle(videoId);
    }

    private void fetchVideoTitle(String videoId) {
        if (!isYouTubeIFrameAPIReady) return;
        player.fetchVideoTitle(videoId);
        lastVideoId = videoId;
    }

    public void onIFrameAPIReady() {
        isYouTubeIFrameAPIReady = true;
    }
    public void onPlayerCreated(String videoTitle, int duration){
        if (!videoTitle.isEmpty()){
            playerManager.onPlayerCreated(new YouTubePlayerData(lastVideoId,videoTitle,duration));
            currentYouTubeTxt.setText(videoTitle);
            toggleYouTubePlayerLayout(layoutPlayYouTubePlayer,layoutCreateYouTubePlayer);
        }else {
            Toast.makeText(activity, "Video Not supported!", Toast.LENGTH_SHORT).show();
        }
        createYouTubePlayer.setEnabled(true);
        createYouTubePlayer.setText(activity.getString(R.string.create_youtube_player));
    }

    public void updateCurrentVideo(String lastVideoId,String videoTitle) {
        this.lastVideoId = lastVideoId;
        currentYouTubeTxt.setText(videoTitle);
        toggleYouTubePlayerLayout(layoutPlayYouTubePlayer,layoutCreateYouTubePlayer);
        createYouTubePlayer.setEnabled(true);
        createYouTubePlayer.setText(activity.getString(R.string.create_youtube_player));
    }

    public void onPlayerReady() {
        activity.runOnUiThread(() -> {
            playYouTubePlayer.setText(activity.getString(R.string.play_youtube_player));
            webView.setVisibility(View.VISIBLE);
            if (isClosed){
                isClosed = false;
                playerManager.requestPlaybackState();
            }else {
                playerManager.playbackToRemote(isPaused,lastPosition);
            }
        });


    }
    private void handleRecreateYouTubePlayerClick(View view) {
        toggleYouTubePlayerLayout(layoutCreateYouTubePlayer,layoutPlayYouTubePlayer);
    }
    private void handleReplayYouTubePlayerClick(View view) {
        toggleYouTubePlayerLayout(layoutPlayYouTubePlayer,layoutCreateYouTubePlayer);
    }

    private void toggleYouTubePlayerLayout(ConstraintLayout layoutToBeVisible, ConstraintLayout layoutToBeHidden) {
        layoutToBeVisible.setVisibility(View.VISIBLE);
        layoutToBeHidden.setVisibility(View.GONE);
    }

    public void playVideo() {
        if (!isYouTubeIFrameAPIReady) return;
        playYouTubePlayer.setText("Playing...");
        if (isClosed){
            lastPosition = 0;
            isPaused = false;
        }
        player.loadVideo(lastVideoId, isPaused ? 0: 1,lastPosition);
    }

    public void onLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }


    public void resetPlayer() {
        player.stop();
        webView.setVisibility(View.GONE);
        toggleYouTubePlayerLayout(layoutCreateYouTubePlayer,layoutPlayYouTubePlayer);
//        currentYouTubeTxt.setText("");
//        lastVideoId = null;
        lastPosition = 0;
        isPaused = false;
        isClosed = true;
    }

    public void onRestart() {
        if (lastVideoId != null && !isClosed) {
            playVideo();
        }
    }

    public void onStop() {
        isPaused = !playerManager.isPlaying();
        player.stop();
    }

    public void onReset(){
        isClosed = true;
        lastVideoId = null;
    }

    public void onRemoteUpdate(String jsonData) {
        playerManager.onRemoteUpdate(jsonData);
    }


}
