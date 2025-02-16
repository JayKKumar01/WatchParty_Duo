package com.github.jaykkumar01.watchparty_duo.managers;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.exoplayer.ReadyEvent;
import com.github.jaykkumar01.watchparty_duo.exoplayer.RemotePlaybackHandler;
import com.github.jaykkumar01.watchparty_duo.gestures.ControlHandler;
import com.github.jaykkumar01.watchparty_duo.interfaces.PlaybackActions;
import com.github.jaykkumar01.watchparty_duo.models.PlaybackState;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerManager {
    private ExoPlayer player;
    private final Activity activity;
    private final RemotePlaybackHandler remotePlaybackHandler;
    private final PlayerView playerView;
    private final AtomicBoolean isRemoteSeek = new AtomicBoolean(false);

    private final ImageView playPauseButton;
    private final ImageView muteButton;
    private final ImageView captionButton;
    private final ImageView fullscreen;
    private Player.Listener seekListener;


    @OptIn(markerClass = UnstableApi.class)
    public PlayerManager(Activity activity, PlayerView playerView) {
        this.activity = activity;
        this.playerView = playerView;
        this.playPauseButton = activity.findViewById(R.id.play_pause);
        this.muteButton = activity.findViewById(R.id.exo_mute_unmute);
        this.captionButton = activity.findViewById(R.id.exo_caption);
        this.fullscreen = activity.findViewById(R.id.exo_screen);
        remotePlaybackHandler = new RemotePlaybackHandler(activity,this);

        setupControls();
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
        remotePlaybackHandler.setPlayer(player);
    }

    private void setupControls() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        muteButton.setOnClickListener(v -> toggleMute());
        captionButton.setOnClickListener(v -> toggleCaptions());
        fullscreen.setOnClickListener(v -> fullScreen());
    }

    public void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            playPauseButton.setImageResource(R.drawable.exo_play);
            playbackToRemote(false);
//            remotePlaybackHandler.playbackToRemote(PlaybackActions.PLAY_PAUSE,false);
        } else {
            player.play();
            playPauseButton.setImageResource(R.drawable.exo_pause);
            playbackToRemote(true);
//            remotePlaybackHandler.playbackToRemote(PlaybackActions.PLAY_PAUSE,true);
        }

    }

    public void toggleMute() {
        if (player.getVolume() > 0f) {
            player.setVolume(0f);
            muteButton.setImageResource(R.drawable.volume_off);
        } else {
            player.setVolume(1f);
            muteButton.setImageResource(R.drawable.volume_on);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void toggleCaptions() {
        if (playerView.getSubtitleView() == null){
            return;
        }
        if(playerView.getSubtitleView().getVisibility() == View.VISIBLE){
            captionButton.setImageResource(R.drawable.cc_off);
            playerView.getSubtitleView().setVisibility(View.GONE);

        }
        else{
            captionButton.setImageResource(R.drawable.cc_on);
            playerView.getSubtitleView().setVisibility(View.VISIBLE);
        }
    }

    public void fullScreen() {
        activity.setRequestedOrientation(
                activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        );
    }

    public void setSeekListener() {
        this.seekListener = new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                if (isRemoteSeek.getAndSet(false)) {
                    return; // Ignore if triggered by remote seek
                }
                if (oldPosition.positionMs == newPosition.positionMs){
                    return;
                }
                // Send seek position to another user
                new ReadyEvent(PlayerManager.this,player).sendSeekPositionToRemote(newPosition.positionMs);
            }
        };
        player.addListener(seekListener);
    }

    public void seekFromRemote(long position) {
        if (player == null || player.getDuration() < position || isRemoteSeek.getAndSet(true)) return;
        player.seekTo(position);
    }


    public void sendSeekPositionToRemote(long position) {
        remotePlaybackHandler.playbackToRemote(PlaybackActions.SEEK, getShiftedPosition(position));
    }

    private long getShiftedPosition(long position){
        long shift = player.isPlaying() ? 500 : 0;
        return Math.max(0, Math.min(position + shift, player.getDuration()));
    }

    public Player.Listener getSeekListener() {
        return seekListener;
    }

    public void onPlaybackUpdate(String jsonData) {
        remotePlaybackHandler.onPlaybackUpdate(jsonData);
    }

    public void requestPlaybackState() {
        remotePlaybackHandler.playbackToRemote(PlaybackActions.REQUEST_PLAYBACK_STATE,null);
    }

    public void remoteUpdatePlayPauseUI(boolean shouldPlay) {
        if (player == null){
            return;
        }
        if (shouldPlay) {
            player.play();
            playPauseButton.setImageResource(R.drawable.exo_pause);
        } else {
            player.pause();
            playPauseButton.setImageResource(R.drawable.exo_play);
            try {
                new ControlHandler(playerView).showControls();
            }catch (Exception e){

            }

        }

    }

    public void playbackToRemote(Boolean isPlaying) {
        if (isPlaying == null){
            isPlaying = player.isPlaying();
        }
        PlaybackState playbackState = new PlaybackState(isPlaying,getShiftedPosition(player.getCurrentPosition()));
        remotePlaybackHandler.playbackToRemote(PlaybackActions.PLAYBACK_STATE, playbackState);
    }
}