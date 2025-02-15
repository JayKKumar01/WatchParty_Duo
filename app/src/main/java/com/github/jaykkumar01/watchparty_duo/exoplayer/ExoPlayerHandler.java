package com.github.jaykkumar01.watchparty_duo.exoplayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.gestures.ControlHandler;
import com.github.jaykkumar01.watchparty_duo.gestures.GestureHandler;
import com.github.jaykkumar01.watchparty_duo.managers.PlayerManager;

public class ExoPlayerHandler {
    private final Context context;
    private final PlayerManager playerManager;
    private ExoPlayer player;
    private final PlayerView playerView;
    private Uri lastMediaUri;
    private long lastPosition = 0;
    private boolean wasPlaying = false;
    private final ControlHandler controlHandler;

    public ExoPlayerHandler(Activity activity) {
        this.context = activity;
        this.playerView = activity.findViewById(R.id.player_view);
        controlHandler = new ControlHandler(playerView);
        playerManager = new PlayerManager(activity,playerView);
        playerView.setOnTouchListener(new GestureHandler(activity, new GestureHandler.OnGestureCallback() {
            @Override
            public void onSingleTap() {
                handleControls();
            }

            @Override
            public void onZoom(boolean isZoomingIn) {
                toggleScaleMode(isZoomingIn);
            }
        }));
    }

    @OptIn(markerClass = UnstableApi.class)
    private void handleControls() {
        if (playerView.isControllerFullyVisible()){
            playerView.hideController();
        }else {
            controlHandler.showControls(2500);
        }
    }


    @OptIn(markerClass = UnstableApi.class)
    private void toggleScaleMode(boolean isZoomingIn) {
        if (playerView == null) {
            return;
        }
        if (isZoomingIn) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM); // Crop mode
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT); // Fit mode
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void playMedia(Uri mediaUri) {
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
            MediaItem mediaItem = MediaItem.fromUri(mediaUri);
            player.setMediaItem(mediaItem);
            playerManager.setPlayer(player);
            playerManager.setSeekListener();
        }

        if (!mediaUri.equals(lastMediaUri)) {
            lastMediaUri = mediaUri;
            lastPosition = 0;  // Reset position if new media
        }

        player.prepare();
        player.seekTo(lastPosition);  // Resume from last position

        playerView.setVisibility(View.VISIBLE);
        playerView.setPlayer(player);
        playerView.hideController();
        player.play();
    }

    public void releasePlayer() {
        wasPlaying = false;
        if (player != null) {
            wasPlaying = true;
            lastPosition = player.getCurrentPosition(); // Save position before release
            if (playerManager.getSeekListener() != null) {
                player.removeListener(playerManager.getSeekListener());
            }
            player.release();
            player = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }

    public void hidePlayer(boolean hidePlayer) {
        playerView.setVisibility(hidePlayer ? View.GONE : View.VISIBLE);
        releasePlayer();
    }

    public void onRestart() {
        if (lastMediaUri != null && wasPlaying) {
            playMedia(lastMediaUri); // Resume previous media
        }
    }

    public void onStop() {
        releasePlayer();
    }

    public void onPlaybackUpdate(String jsonData) {
        playerManager.onPlaybackUpdate(jsonData);
    }
}
