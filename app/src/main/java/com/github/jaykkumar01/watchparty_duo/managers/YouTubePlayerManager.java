package com.github.jaykkumar01.watchparty_duo.managers;

import android.app.Activity;
import android.util.Log;

import com.github.jaykkumar01.watchparty_duo.interfaces.RemoteActions;
import com.github.jaykkumar01.watchparty_duo.interfaces.YouTubePlayerEvents;
import com.github.jaykkumar01.watchparty_duo.models.YouTubePlaybackState;
import com.github.jaykkumar01.watchparty_duo.models.YouTubePlayerData;
import com.github.jaykkumar01.watchparty_duo.youtubeplayer.YouTubePlayer;
import com.github.jaykkumar01.watchparty_duo.youtubeplayer.YouTubePlayerHandler;
import com.github.jaykkumar01.watchparty_duo.youtubeplayer.YouTubeRemoteActionHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class YouTubePlayerManager implements YouTubePlayerEvents, RemoteActions {
    private static final int INTERNET_DELAY_OFFSET = 2;
    private final YouTubePlayerHandler handler;
    private final YouTubeRemoteActionHandler actionHandler;
    private YouTubePlayer player;
    private int lastEvent = -1;
    private boolean isFirstPlay;
    private final AtomicBoolean isRemoteAction = new AtomicBoolean(true);

    private int duration;

    public YouTubePlayerManager(Activity activity, YouTubePlayerHandler handler) {
        this.handler = handler;
        this.actionHandler = new YouTubeRemoteActionHandler(activity,this);
    }

    public void setPlayer(YouTubePlayer player){
        this.player = player;
    }

    public void onPlay(int timeMs) {
        if (!isFirstPlay){
            isFirstPlay = true;
            handler.onPlayerReady();
        }
        lastEvent = PLAYING;
        Log.d("YouTubePlayerManager", "Playing at " + timeMs + " ms.");

        if (isRemoteAction.getAndSet(false)) {
            return; // Ignore if triggered by remote seek
        }

        playbackToRemote(true,timeMs);

    }

    public void onPause(int timeMs) {
        lastEvent = PAUSED;
        Log.d("YouTubePlayerManager", "Paused at " + timeMs + " ms.");

        if (isRemoteAction.getAndSet(false)) {
            return; // Ignore if triggered by remote seek
        }

        playbackToRemote(false,timeMs);
    }

    public void onSeek(int timeMs) {
        lastEvent = SEEK;
        Log.d("YouTubePlayerManager", "Seeked to " + timeMs + " ms.");
    }

    public boolean isPlaying() {
        return lastEvent == PLAYING;
    }

    public void onRemoteUpdate(String jsonData) {
        actionHandler.onActionUpdate(jsonData);
    }

    public void onPlayerCreated(YouTubePlayerData data) {
        duration = data.getDuration();
        actionHandler.actionToRemote(YOUTUBE_CURRENT_VIDEO,data);
    }

    public void updateCurrentVideo(String lastVideoId,String videoTitle) {
        handler.updateCurrentVideo(lastVideoId,videoTitle);
    }

    public void requestPlaybackState() {
        actionHandler.actionToRemote(YOUTUBE_REQUEST_PLAYBACK_STATE,null);
    }

    public void requestPlaybackFromPlayerForRemote() {
        if (player == null){
            return;
        }
        player.requestPlayback();
    }

    public void playbackToRemote(boolean isPlaying, int currentPosition) {
        int modifiedTimeSecond = currentPosition + INTERNET_DELAY_OFFSET;
        if (modifiedTimeSecond > duration || !isPlaying){
            modifiedTimeSecond = currentPosition;
        }
        actionHandler.actionToRemote(YOUTUBE_PLAYBACK_STATE,new YouTubePlaybackState(isPlaying,modifiedTimeSecond));
    }

    public void playbackFromRemote(boolean isPlaying, int currentPosition) {
        if (player == null){
            return;
        }
        if (isRemoteAction.getAndSet(true)) return;

        player.updatePlayback(isPlaying,currentPosition);
    }

    public void resetPlayer(int lastPosition) {
        onPause(lastPosition);
        player = null;
        lastEvent = -1;
        isFirstPlay = false;
    }
}

