package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.dynamic.SettingsUI;
import com.github.jaykkumar01.watchparty_duo.dialogs.BackPressHandler;
import com.github.jaykkumar01.watchparty_duo.helpers.FeedNotificationHelper;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class FeedActivity extends AppCompatActivity {
    private static WeakReference<FeedActivity> instanceRef;

    public static FeedActivity getInstance() {
        return (instanceRef != null) ? instanceRef.get() : null;
    }

    // UI Components
    private ScrollView logScrollView;
    private TextView logTextView;
    private ConstraintLayout layoutConnect;
    private ConstraintLayout layoutJoin;
    private TextInputEditText etJoinName;
    private TextInputEditText etCode;
    private TextView tvName;
    private AppCompatButton btnConnect;
    private AppCompatButton btnJoin;

    // Utility
    private LogUpdater logUpdater;

    // Data Variables
    private String userName;
    private String peerId;
    private String remoteId;
    private boolean isOpeningPlayerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BackPressHandler.registerBackPressHandler(this,getOnBackPressedDispatcher(), Gravity.BOTTOM);
        instanceRef = new WeakReference<>(this);
        initViews();
        setupLogUpdater();
        setupScrollListener();
        FeedNotificationHelper.createNotificationChannel(this);
    }

    private void initViews() {
        logScrollView = findViewById(R.id.logScrollView);
        logTextView = findViewById(R.id.logTextView);
        layoutConnect = findViewById(R.id.layoutConnect);
        layoutJoin = findViewById(R.id.layoutJoin);
        etJoinName = findViewById(R.id.etJoinName);
        etCode = findViewById(R.id.etCode);
        tvName = findViewById(R.id.tvName);
        btnConnect = findViewById(R.id.btnConnect);
        btnJoin = findViewById(R.id.btnJoin);
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

    public void connect(View view) {
        if (!PermissionHandler.hasPermission(this)) return;
        btnConnect.setText(R.string.connecting);
        btnConnect.setEnabled(false);

        userName = Objects.requireNonNull(etJoinName.getText()).toString().trim();
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            resetConnectButton();
            return;
        }

        startFeedService();
        addLog("Starting Feed Service...");

        new Handler().postDelayed(new Runnable() {
            private int secondsLeft = 10;

            @Override
            public void run() {
                if (peerId != null) {  // If peerId is assigned, stop the countdown
                    addLog("Peer successfully opened: " + peerId);
                    return;
                }

                if (secondsLeft > 0) {
                    addLog("Generating peer ... (Timeout in " + secondsLeft + " sec)");
                    secondsLeft--;
                    new Handler().postDelayed(this, 1000); // Repeat every second
                } else {
                    // If still null, timeout
                    stopFeedService();
                    resetConnectButton();
                    addLog("Connection timeout. Please try again.");
                    Toast.makeText(FeedActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }, 0);


    }

    private void stopFeedService() {
        Intent serviceIntent = new Intent(this, FeedService.class);
        stopService(serviceIntent);
        addLog("Feed Service Stopped.");
    }


    // Join Logic
    // Join Logic
    public void join(View view) {
        remoteId = null;
        btnJoin.setText(R.string.joining);
        btnJoin.setEnabled(false);

        String remoteId = Objects.requireNonNull(etCode.getText()).toString().trim();
        if (remoteId.isEmpty()) {
            Toast.makeText(this, "Please enter Peer ID.", Toast.LENGTH_SHORT).show();
            resetJoinButton();
            return;
        }

        FeedService feedService = FeedService.getInstance();
        if (feedService == null) {
            addLog("FeedService not running!");
            resetJoinButton();
            return;
        }

        feedService.connect(remoteId);

        new Handler().postDelayed(new Runnable() {
            private int secondsLeft = 5;

            @Override
            public void run() {
                if (FeedActivity.this.remoteId != null) { // Stop countdown if connected
                    addLog("Connected to " + FeedActivity.this.remoteId);
                    return;
                }

                if (secondsLeft > 0) {
                    addLog("Waiting for connection... (Timeout in " + secondsLeft + " sec)");
                    secondsLeft--;
                    new Handler().postDelayed(this, 1000);
                } else {
                    resetJoinButton();
                    addLog("Connection timeout. Please try again.");
                    Toast.makeText(FeedActivity.this, "Connection timeout. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }, 0);
    }


    private void resetConnectButton() {
        btnConnect.setText(R.string.connect);
        btnConnect.setEnabled(true);
    }

    private void resetJoinButton() {
        btnJoin.setText(R.string.join);
        btnJoin.setEnabled(true);
    }

    public void onPeerOpen(String peerId) {
        this.peerId = peerId;
        runOnUiThread(() -> updateUI(peerId));
    }

    private void updateUI(String peerId) {
        addLog("Peer Opened: " + peerId);
        Base.hideKeyboard(this);
        tvName.setText(String.format("Welcome %s, Your ID: %s-%s", userName, peerId.substring(0, 3), peerId.substring(3, 6)));
        layoutConnect.setVisibility(View.GONE);
        layoutJoin.setVisibility(View.VISIBLE);
        resetConnectButton();
    }



    public void onConnectionOpen(String peerId, String remoteId) {
        this.peerId = peerId;
        this.remoteId = remoteId;
        runOnUiThread(() -> updateConnectionUI(peerId, remoteId));
    }

    private void updateConnectionUI(String peerId, String remoteId) {
        addLog(String.format("Peer: %s, Remote: %s", peerId, remoteId));
        Base.hideKeyboard(this);
        layoutJoin.setVisibility(View.GONE);
        resetJoinButton();
        launchPlayerActivity();
    }


    private void launchPlayerActivity(){
        Intent intent = new Intent(this, PlayerActivity.class);
        // Add extras to the intent
        intent.putExtra(Constants.PEER, new PeerModel(userName,peerId,remoteId));
        isOpeningPlayerActivity = true;
        startActivity(intent);
        finish();
    }


    // Method to hide keyboard



    private void startFeedService() {
        Intent serviceIntent = new Intent(this, FeedService.class);
        startService(serviceIntent);
    }

    public void addLog(String message) {
        if (logUpdater != null) {
            logUpdater.addLogMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        RefHelper.reset(instanceRef);
        if (!isOpeningPlayerActivity){
            if (FeedService.getInstance() != null){
                FeedService.getInstance().stopService();
            }
        }
        super.onDestroy();
    }

    SettingsUI.SettingsUICallback callback = new SettingsUI.SettingsUICallback() {
        @Override
        public void onSubmit(String result) {
            addLog(result);
        }
    };
    public void settings(View view) {
        SettingsUI.showSettingsDialog(this,callback);
    }


    public void changeVideoSettings(View view) {
        SettingsUI.showSettingsDialog(this, this::addLog);
    }
}