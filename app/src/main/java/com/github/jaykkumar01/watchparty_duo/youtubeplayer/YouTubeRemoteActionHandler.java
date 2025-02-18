package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import com.github.jaykkumar01.watchparty_duo.interfaces.RemoteActions;
import com.github.jaykkumar01.watchparty_duo.managers.YouTubePlayerManager;
import com.github.jaykkumar01.watchparty_duo.models.YouTubePlaybackState;
import com.github.jaykkumar01.watchparty_duo.models.YouTubePlayerData;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class YouTubeRemoteActionHandler implements RemoteActions {
    private final YouTubePlayerManager playerManager;
    private final Gson gson = new Gson();
    private final Activity activity;

    public YouTubeRemoteActionHandler(Activity activity, YouTubePlayerManager playerManager) {
        this.activity = activity;
        this.playerManager = playerManager;
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

        switch (action) {
            case YOUTUBE_CURRENT_VIDEO:
                YouTubePlayerData data = gson.fromJson(gson.toJson(object), YouTubePlayerData.class);
                playerManager.updateCurrentVideo(data.getLastVideoId(),data.getVideoTitle());
                break;
            case YOUTUBE_REQUEST_PLAYBACK_STATE:
                playerManager.requestPlaybackFromPlayerForRemote();
                break;
            case YOUTUBE_PLAYBACK_STATE:
                YouTubePlaybackState playbackState = gson.fromJson(gson.toJson(object),YouTubePlaybackState.class);
                playerManager.playbackFromRemote(playbackState.isPlaying(),playbackState.getCurrentPosition());

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
