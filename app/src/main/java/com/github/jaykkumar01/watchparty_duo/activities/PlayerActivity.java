package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.helpers.CircularOutlineProvider;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.imagefeed.ImageFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;

import java.lang.ref.WeakReference;

public class PlayerActivity extends AppCompatActivity implements FeedListener {

    private static WeakReference<PlayerActivity> instanceRef;
    private ImageFeed imageFeed;
    private ImageView micImageView;
    private ImageView deafenImageView;

    public static PlayerActivity getInstance() {
        return instanceRef != null ? instanceRef.get() : null;
    }


    private PlayerView playerView;
    private TextView userName;
    private ExoPlayer player;
    private ActivityResultLauncher<String> pickVideoLauncher;

    private PeerModel peerModel;
    private TextureView peerFeedTextureView,remoteFeedTextureView;

    private ScrollView logScrollView;
    private TextView logTextView;
    private LogUpdater logUpdater;

    boolean isMute = true;
    boolean isDeafen;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        instanceRef = new WeakReference<>(this);

        initViews();

        peerFeedTextureView.setOutlineProvider(new CircularOutlineProvider());
        peerFeedTextureView.setClipToOutline(true);

        setupLogUpdater();
        setupScrollListener();

        // Retrieve extras from the intent
        Intent intent = getIntent();
        if (intent != null) {
            peerModel = (PeerModel) intent.getSerializableExtra(Constants.PEER);
            if (peerModel != null) {
                userName.setText(peerModel.getName());
            }
        }


        setupPickVideoLauncher();

        FeedService feedService = FeedService.getInstance();
        if (feedService != null){
            feedService.setFeedSurfaces(peerFeedTextureView,remoteFeedTextureView);
        }

    }

    private void setupLogUpdater() {
        logUpdater = new LogUpdater(logTextView, logScrollView);
        logUpdater.addLogMessage("Check logs here...");
    }

    private void setupScrollListener() {
        logScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            boolean isUserScrolling = logScrollView.getScrollY() < logTextView.getHeight() - logScrollView.getHeight();
            logUpdater.setUserScrolling(isUserScrolling);
        });
    }

    public void addLog(String message) {
        if (logUpdater != null) {
            logUpdater.addLogMessage(message);
        }
    }

    private void initViews() {
        logScrollView = findViewById(R.id.logScrollView);
        logTextView = findViewById(R.id.logTextView);
//        playerView = findViewById(R.id.player_view);
        userName = findViewById(R.id.userName);
        peerFeedTextureView = findViewById(R.id.peerFeed);
        remoteFeedTextureView = findViewById(R.id.remoteFeed);

        micImageView = findViewById(R.id.micBtn);
        deafenImageView = findViewById(R.id.deafenBtn);
    }
    private void setupPickVideoLauncher() {
        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::initializePlayer
        );
    }

    private void initializePlayer(Uri uri) {
    }




    public void refreshLayout(View view) {
    }

    public void offlineAddLayout(View view) {
    }

    public void onlineAddLayout(View view) {
    }

    public void selectVideo(View view) {
        pickVideoLauncher.launch("video/*");
    }

    public void startSyncPlay(View view) {
    }

    public void createYouTubeUrl(View view) {
    }

    public void joinYouTubeVideo(View view) {
    }

    public void toggleLayout(View view) {
    }

    public void closeLive(View view) {
    }

    public void mic(View view) {
        isMute = !isMute;
        setIsMute(isMute);
    }
    public void deafen(View view) {
        isDeafen = !isDeafen;
        setIsDeafen(isDeafen);
    }
    private void setIsMute(boolean isMute){
        if (!isMute && isDeafen){
            isDeafen = false;
            setIsDeafen(false);
        }

        micImageView.setImageResource(isMute ? R.drawable.mic_off : R.drawable.mic_on);
        FeedService feedService = FeedService.getInstance();
        if (feedService != null){
            feedService.muteAudio(isMute);
        }
    }

    private void setIsDeafen(boolean isDeafen){
        if (isDeafen){
            setIsMute(true);
        }else if (!isMute){
            setIsMute(false);
        }
        deafenImageView.setImageResource(isDeafen ? R.drawable.deafen_on : R.drawable.deafen_off);
        FeedService feedService = FeedService.getInstance();
        if (feedService != null){
            feedService.deafenAudio(isDeafen);
        }
    }

    public void endCall(View view) {
        Intent intent = new Intent(this, FeedActivity.class);
        finish();
        startActivity(intent);
    }

    public void onConnectionClosed() {
        addLog("Connection Closed. Returning to Homepage in 3 seconds...");
        Toast.makeText(this, "Connection Closed", Toast.LENGTH_SHORT).show();

        Runnable countdownRunnable = new Runnable() {
            int secondsLeft = 3;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    addLog("Returning to Homepage in " + secondsLeft + (secondsLeft == 1 ? " second..." : " seconds..."));
                    secondsLeft--;
                    handler.postDelayed(this, 1000);
                } else {
                    endCall(null);
                }
            }
        };

        handler.post(countdownRunnable);
    }


    public void sendMessage(View view) {
    }


    @Override
    public void onFeed(byte[] bytes, long millis, int feedType) {

    }

    @Override
    public void onError(String err) {

    }

    @Override
    public void onUpdate(String logMessage) {

    }

    @Override
    protected void onDestroy() {
        if (FeedService.getInstance() != null){
            FeedService.getInstance().stopService();
        }
        RefHelper.reset(instanceRef);
        super.onDestroy();
        Log.d("PlayerActivity", "onDestroy called! Is finishing: " + isFinishing());
    }


    @Override
    protected void onRestart() {
        notifyFeedService(true);
        super.onRestart();
    }

    @Override
    protected void onStop() {
        notifyFeedService(false);
        super.onStop();
    }

    private void notifyFeedService(boolean isRestarting) {
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.onActivityStateChanged(isRestarting);
        }
    }


}
