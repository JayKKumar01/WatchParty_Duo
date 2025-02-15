package com.github.jaykkumar01.watchparty_duo.gestures;

import android.os.Handler;

import androidx.annotation.OptIn;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.github.jaykkumar01.watchparty_duo.R;

public class ControlHandler {
    private static final int CTRL_HIDE_TIMEOUT = 2500;
    private final Handler handler = new Handler();
    private final PlayerView playerView;
    private long touchActive = System.currentTimeMillis();

    public ControlHandler(PlayerView playerView){
        this.playerView = playerView;
        ConstraintLayout ctrlLayout = playerView.findViewById(R.id.ctrlLayout);
//        ctrlLayout.setOnClickListener(v -> touchActive = System.currentTimeMillis());
    }


    @OptIn(markerClass = UnstableApi.class)
    public void showControls() {
        playerView.showController();
        touchActive = System.currentTimeMillis();
        handler.postDelayed(() -> {
            if(playerView.isControllerFullyVisible() && (System.currentTimeMillis() - touchActive) >= CTRL_HIDE_TIMEOUT){
                playerView.hideController();
            }
        }, CTRL_HIDE_TIMEOUT);
    }
}
