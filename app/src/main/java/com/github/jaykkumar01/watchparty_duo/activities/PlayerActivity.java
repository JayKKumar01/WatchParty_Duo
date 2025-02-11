package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
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

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;

import java.lang.ref.WeakReference;

public class PlayerActivity extends AppCompatActivity {

    // Static reference to the activity
    private static WeakReference<PlayerActivity> instanceRef;

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
    private ActivityResultLauncher<String> pickVideoLauncher;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // State variables
    private boolean isMute = true;
    private boolean isDeafen = false;
    private boolean isVideo = true;

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

        setupUI();
        setupListeners();
        setupPickVideoLauncher();

        // Retrieve peer info
        Intent intent = getIntent();
        if (intent != null) {
            PeerModel peerModel = (PeerModel) intent.getSerializableExtra(Constants.PEER);
            if (peerModel != null) {
                friendName.setText(peerModel.getName());
            }
        }

        // Connect feed surfaces to FeedService
        FeedService feedService = FeedService.getInstance();
        if (feedService != null) {
            feedService.setFeedSurfaces(peerFeedTextureView, remoteFeedTextureView);
        }
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

    private void setupPickVideoLauncher() {
        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::initializePlayer
        );
    }

    private void initializePlayer(Uri uri) {
        // TODO: Implement video player initialization
    }

    public void selectVideo(View view) {
        pickVideoLauncher.launch("video/*");
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
                    addLog("Returning to Homepage in " + secondsLeft + " second(s)...");
                    secondsLeft--;
                    handler.postDelayed(this, 1000);
                } else {
                    endCall(null);
                }
            }
        };
        handler.post(countdownRunnable);
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
