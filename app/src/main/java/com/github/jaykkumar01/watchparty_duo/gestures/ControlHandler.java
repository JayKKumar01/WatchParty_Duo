package com.github.jaykkumar01.watchparty_duo.gestures;

import android.os.Handler;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

public class ControlHandler {
    private final Handler handler = new Handler();
    private final PlayerView playerView;
    private long touchActive = System.currentTimeMillis();

    public ControlHandler(PlayerView playerView){
        this.playerView = playerView;
    }


    @OptIn(markerClass = UnstableApi.class)
    public void showControls(int controlHideTime) {
        playerView.showController();
        touchActive = System.currentTimeMillis();
        handler.postDelayed(() -> {
            if(playerView.isControllerFullyVisible() && (System.currentTimeMillis() - touchActive) >= controlHideTime){
                playerView.hideController();
            }
        }, controlHideTime);
    }
}
