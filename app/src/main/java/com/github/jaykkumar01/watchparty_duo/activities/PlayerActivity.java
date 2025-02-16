package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.dialogs.BackPressHandler;
import com.github.jaykkumar01.watchparty_duo.dialogs.ConnectionDialogHandler;
import com.github.jaykkumar01.watchparty_duo.dialogs.ExitDialogHandler;
import com.github.jaykkumar01.watchparty_duo.exoplayer.ExoPlayerHandler;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.playeractivityhelpers.MediaHandler;
import com.github.jaykkumar01.watchparty_duo.playeractivityhelpers.PlayerOrientationHandler;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.utils.AspectRatio;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;

import java.lang.ref.WeakReference;

public class PlayerActivity extends AppCompatActivity {

    // Static reference to the activity
    private static WeakReference<PlayerActivity> instanceRef;
    private ExitDialogHandler exitDialogHandler;
    private ExoPlayerHandler exoPlayerHandler;

    public static PlayerActivity getInstance() {
        return instanceRef != null ? instanceRef.get() : null;
    }

    // UI Components
    private ImageView micImageView, videoImageView, deafenImageView;
    private TextView friendName, logTextView;
    private TextureView peerFeedTextureView, remoteFeedTextureView;
    private ScrollView logScrollView;

    // Other components
    private LogUpdater logUpdater;

    // State variables
    private boolean isMute = true;
    private boolean isDeafen = false;
    private boolean isVideo = true;

    private ConnectionDialogHandler connectionDialogHandler;

    private PlayerOrientationHandler playerOrientationHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        AspectRatio.set(this);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.theme_related));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BackPressHandler.registerBackPressHandler(this,getOnBackPressedDispatcher(), Gravity.CENTER);

        instanceRef = new WeakReference<>(this);

        setupUI();
        setupListeners();

        // Retrieve peer info
        Intent intent = getIntent();
        if (intent != null) {
            PeerModel peerModel = (PeerModel) intent.getSerializableExtra(Constants.PEER);
            if (peerModel != null) {
                friendName.setText(peerModel.getName());
            }
        }


        connectionDialogHandler = new ConnectionDialogHandler(this);
        exitDialogHandler = new ExitDialogHandler(this);

        playerOrientationHandler = new PlayerOrientationHandler(this,remoteFeedTextureView,peerFeedTextureView);
        playerOrientationHandler.handleOrientationChange(getResources().getConfiguration().orientation);
        exoPlayerHandler = new ExoPlayerHandler(this);
        new MediaHandler(this,exoPlayerHandler);
    }



    private void setupUI() {
        logScrollView = findViewById(R.id.logScrollView);
        logTextView = findViewById(R.id.logTextView);
        friendName = findViewById(R.id.remotePeerName);
        peerFeedTextureView = findViewById(R.id.peerFeed);
        remoteFeedTextureView = findViewById(R.id.remoteFeed);
        micImageView = findViewById(R.id.micBtn);
        deafenImageView = findViewById(R.id.deafenBtn);
        videoImageView = findViewById(R.id.videoBtn);

        logUpdater = new LogUpdater(logTextView, logScrollView);
        logUpdater.addLogMessage("Check logs here...");
    }

    private void setupListeners() {
        logScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            boolean isUserScrolling = logScrollView.getScrollY() < logTextView.getHeight() - logScrollView.getHeight();
            logUpdater.setUserScrolling(isUserScrolling);
        });
    }


    public void video(View view) {
        isVideo = !isVideo;
        updateVideoState();
    }

    private void updateVideoState() {
        videoImageView.setImageResource(isVideo ? R.mipmap.video_on_foreground : R.mipmap.video_off_foreground);
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.isVideo(isVideo);
        }
    }

    public void mic(View view) {
        if(isDeafen && !isMute){
            isMute = true;
        }
        isMute = !isMute;
        updateMicState(isMute);
    }

    public void deafen(View view) {
        isDeafen = !isDeafen;
        updateDeafenState(isDeafen);
    }

    private void updateMicState(boolean isMute) {

        if (!isMute && isDeafen) {
            isDeafen = false;
            updateDeafenState(false);
        }

        micImageView.setImageResource(isMute ? R.drawable.mic_off : R.drawable.mic_on);
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.muteAudio(isMute);
        }
    }

    private void updateDeafenState(boolean isDeafen) {

        if (isDeafen) {
            if (!isMute) {
                updateMicState(true);
            }
        } else if (!isMute) {
            updateMicState(false);
        }


        deafenImageView.setImageResource(isDeafen ? R.drawable.deafen_on : R.drawable.deafen_off);
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.deafenAudio(isDeafen);
        }
    }

    public void goToHomepage(View view) {
        Intent intent = new Intent(this, FeedActivity.class);
        finish();
        startActivity(intent);
    }

    public void onConnectionClosed() {
        exitDialogHandler.showExitDialog();
    }

    public void addLog(String message) {
        if (logUpdater != null) {
            logUpdater.addLogMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.stopService();
        }
        RefHelper.reset(instanceRef);
        exitDialogHandler.dismissExitDialog();
        exoPlayerHandler.releasePlayer();
        super.onDestroy();
        Log.d("PlayerActivity", "onDestroy called! Is finishing: " + isFinishing());
    }

    @Override
    protected void onRestart() {
        notifyImageFeedService(true);
        exoPlayerHandler.onRestart();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        notifyImageFeedService(false);
        exoPlayerHandler.onStop();
        super.onStop();
    }

    private void notifyImageFeedService(boolean isRestarting) {
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.onActivityStateChanged(isRestarting,isVideo);
        }
    }


    public void onConnectionStatus(boolean isConnectionAlive) {
        //exoPlayerHandler.onConnectionStatus(isConnectionAlive);

        if (isConnectionAlive){
            //connectionDialogHandler.dismissConnectingDialog();
        }else {
           // connectionDialogHandler.showConnectingDialog();
        }

    }

    public void onRestartPeer() {
        //connectionDialogHandler.updateConnectingDialog(1);
    }
    public void onPeerError() {
        //connectionDialogHandler.updateConnectingDialog(0);
    }

    public void onRestartConnection() {
        //connectionDialogHandler.updateConnectingDialog(2);
        //exoPlayerHandler.onConnectionStatus(true);
    }

    public void onPeerRetryLimitReached() {
        //connectionDialogHandler.dismissConnectingDialog();
        goToHomepage(null);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        playerOrientationHandler.handleOrientationChange(newConfig.orientation);
    }


    public void onPlaybackUpdate(String jsonData) {
        exoPlayerHandler.onPlaybackUpdate(jsonData);
    }


}
