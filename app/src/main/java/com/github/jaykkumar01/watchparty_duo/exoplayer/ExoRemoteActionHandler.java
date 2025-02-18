package com.github.jaykkumar01.watchparty_duo.exoplayer;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import androidx.media3.exoplayer.ExoPlayer;

import com.github.jaykkumar01.watchparty_duo.interfaces.RemoteActions;
import com.github.jaykkumar01.watchparty_duo.managers.PlayerManager;
import com.github.jaykkumar01.watchparty_duo.models.PlaybackState;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ExoRemoteActionHandler {
    private ExoPlayer player;
    private final PlayerManager playerManager;
    private final Gson gson = new Gson();
    private final Activity activity;

    public ExoRemoteActionHandler(Activity activity, PlayerManager playerManager) {
        this.activity = activity;
        this.playerManager = playerManager;
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public void actionToRemote(int action, Object object) {
        FeedService feedService = FeedService.getInstance();
        if (feedService != null){
            feedService.playbackToRemote(action, object);
        }
        Log.d("RemoteAction", "Sending action: " + action + " | Value: " + object);
        // 🔹 Implement actual network logic here
    }

    private void actionFromRemote(int action, Object object) {
        Log.d("RemoteAction", "Received action: " + action + " | Value: " + object);

        if (player == null) {
            Log.e("RemoteAction", "Player instance is null!");
            return;
        }

        switch (action) {
            case RemoteActions.EXO_PLAY_PAUSE:
                playerManager.remoteUpdatePlayPauseUI((Boolean) object);
                break;

            case RemoteActions.EXO_SEEK:
                playerManager.seekFromRemote(((Double) object).longValue());
                break;

            case RemoteActions.EXO_REQUEST_PLAYBACK_STATE:
                playerManager.playbackToRemote(null);
                break;

            case RemoteActions.EXO_PLAYBACK_STATE:
                if (object == null){
                    return;
                }
                PlaybackState model = gson.fromJson(gson.toJson(object), PlaybackState.class);
                playerManager.remoteUpdatePlayPauseUI(model.isPlaying());
                playerManager.seekFromRemote(model.getPosition());

            default:
                Log.w("RemotePlayback", "Unknown action received: " + action);
        }
    }

    public void onActionUpdate(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        // Convert JSON string to a Map
        Pair<Integer,Object> pair = gson.fromJson(
                jsonData,
                new TypeToken<Pair<Integer, Object>>() {}.getType()
        );

        activity.runOnUiThread(() -> actionFromRemote(pair.first,pair.second));

    }
}
