package com.github.jaykkumar01.watchparty_duo.exoplayer;

import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import com.github.jaykkumar01.watchparty_duo.managers.PlayerManager;
import com.github.jaykkumar01.watchparty_duo.models.PlaybackState;

public class ReadyEvent implements Player.Listener {
    private final ExoPlayer player;
    private final PlayerManager playerManager;

    public ReadyEvent(PlayerManager playerManager, ExoPlayer player) {
        this.playerManager = playerManager;
        this.player = player;
    }

    public void requestPlaybackState() {
        addListener(playerManager::requestPlaybackState);
    }

    public void playbackToRemote(boolean isPlaying, long currentPosition) {
        addListener(() -> playerManager.playbackToRemote(new PlaybackState(isPlaying, currentPosition)));
    }

    private void addListener(Runnable action) {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    action.run();
                    player.removeListener(this);
                }
            }
        });
    }
}
