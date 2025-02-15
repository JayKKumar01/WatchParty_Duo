package com.github.jaykkumar01.watchparty_duo.exoplayer;

import android.media.Image;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import androidx.media3.exoplayer.ExoPlayer;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.interfaces.PlaybackActions;
import com.github.jaykkumar01.watchparty_duo.managers.PlayerManager;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class RemotePlaybackHandler {
    private ExoPlayer player;
    private final PlayerManager playerManager;
    private final ImageView playPauseButton;
    private final Gson gson = new Gson();

    public RemotePlaybackHandler(PlayerManager playerManager, ImageView playPauseButton) {
        this.playerManager = playerManager;
        this.playPauseButton = playPauseButton;
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public void playbackToRemote(int action, Object value) {
        FeedService feedService = FeedService.getInstance();
        if (feedService != null){
            feedService.playbackToRemote(action, value);
        }
        Log.d("RemotePlayback", "Sending action: " + action + " | Value: " + value);
        // 🔹 Implement actual network logic here
    }

    public void playbackFromRemote(int action, Object value) {
        Log.d("RemotePlayback", "Received action: " + action + " | Value: " + value);

        if (player == null) {
            Log.e("RemotePlayback", "Player instance is null!");
            return;
        }

        switch (action) {
            case PlaybackActions.PLAY_PAUSE:
                if (value instanceof Boolean) {
                    boolean shouldPlay = (Boolean) value;
                    if (shouldPlay) {
                        player.play();
                        playPauseButton.setImageResource(R.drawable.exo_pause);
                    } else {
                        player.pause();
                        playPauseButton.setImageResource(R.drawable.exo_play);
                    }
                }
                break;

            case PlaybackActions.SEEK:
                if (value instanceof Long) {
                    playerManager.seekToRemote((Long) value);
                }
                break;

            default:
                Log.w("RemotePlayback", "Unknown action received: " + action);
        }
    }

    public void onPlaybackUpdate(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        // Convert JSON string to a Map
        Pair<Integer,Object> pair = gson.fromJson(
                jsonData,
                new TypeToken<Pair<Integer, Object>>() {}.getType()
        );

        playbackFromRemote(pair.first,pair.second);

    }
}

