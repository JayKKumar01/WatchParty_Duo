package com.github.jaykkumar01.watchparty_duo.feed;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.github.jaykkumar01.watchparty_duo.activities.PlayerActivity;
import com.github.jaykkumar01.watchparty_duo.constants.FeedServiceInfo;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.github.jaykkumar01.watchparty_duo.webfeed.WebFeedHelper;
import com.google.android.material.textfield.TextInputEditText;

public class FeedActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static FeedActivity instance;
    public static FeedActivity getInstance() {
        return instance;
    }

    private ConstraintLayout mainLayout;
    private ScrollView logScrollView;
    private TextView logTextView;
    private ConstraintLayout layoutConnection;
    private ConstraintLayout layoutConnect;
    private ConstraintLayout layoutJoin;
    private TextInputEditText etJoinName;
    private TextInputEditText etCode;
    private TextView tvName;
    private AppCompatButton btnConnect;
    private AppCompatButton btnJoin;

    private LogUpdater logUpdater;
    private String userName;
    private String peerId;
    private String remoteId;

    boolean isTimeout;

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

        instance = this;
        initViews();
        setupLogUpdater();
        setupScrollListener();
        createNotificationChannel();
    }

    private void initViews() {
        mainLayout = findViewById(R.id.main);
        logScrollView = findViewById(R.id.logScrollView);
        logTextView = findViewById(R.id.logTextView);
        layoutConnection = findViewById(R.id.layoutConnection);
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
        logUpdater.addLogMessage("Log system initialized.");
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

        userName = etJoinName.getText().toString().trim();
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            resetConnectButton();
            return;
        }

        startFeedService();
        addLog("Starting Feed Service...");

        // Set timeout logic
        isTimeout = false;
        new Handler().postDelayed(() -> {
            if (peerId == null) {  // Check if peerId is still null, meaning no connection was established
                isTimeout = true;
                stopFeedService();
                resetConnectButton();
                addLog("Connection timeout. Please try again.");
                Toast.makeText(this, "Connection timeout. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }, 5000); // 5-second timeout
    }

    private void stopFeedService() {
        Intent serviceIntent = new Intent(this, FeedService.class);
        stopService(serviceIntent);
        addLog("Feed Service Stopped.");
    }


    // Join Logic
    public void join(View view) {
        remoteId = null;
        btnJoin.setText(R.string.joining);
        btnJoin.setEnabled(false);

        String remoteId = etCode.getText().toString().trim();
        if (remoteId.isEmpty()) {
            Toast.makeText(this, "Please enter Peer ID.", Toast.LENGTH_SHORT).show();;
            resetJoinButton();
            return;
        }
        FeedService feedService = FeedService.getInstance();
        if (feedService == null){
            addLog("FeedService not running!");
            return;
        }
        feedService.connect(remoteId);
        addLog("Connecting to "+remoteId);

        new Handler().postDelayed(() -> {
            if (this.remoteId == null) {  // Check if peerId is still null, meaning no connection was established
                resetJoinButton();
                addLog("Connection timeout. Please try again.");
                Toast.makeText(this, "Connection timeout. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }, 5000); // 5-second timeout

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
        if (isTimeout){
            return;
        }
        this.peerId = peerId;
        runOnUiThread(() -> {
            addLog("Peer Opened: " + peerId);
            hideKeyboard();
            tvName.setText("Welcome " + userName + ", Your ID: " + peerId);
            layoutConnect.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            resetConnectButton();
        });
    }

    public void onConnectionOpen(String peerId, String remoteId) {
        this.peerId = peerId;
        this.remoteId = remoteId;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addLog("Peer: " + peerId + ", Remote: " + remoteId);
                hideKeyboard();
                layoutJoin.setVisibility(View.GONE);
                resetJoinButton();
                launchPlayerActivity();
            }
        });
    }

    private void launchPlayerActivity(){
        Intent intent = new Intent(this, PlayerActivity.class);
        // Add extras to the intent
        intent.putExtra(Constants.PEER, new PeerModel(userName,peerId,remoteId));
        finish(); // Destroy the current activity before launching the new one
        startActivity(intent);
    }


    // Method to hide keyboard
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    FeedServiceInfo.CHANNEL_ID,
                    FeedServiceInfo.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(FeedServiceInfo.CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void startFeedService() {
        Intent serviceIntent = new Intent(this, FeedService.class);
        startService(serviceIntent);
    }

    public void addLog(String message) {
        if (logUpdater != null) {
            logUpdater.addLogMessage(message);
        }
    }

    public void onPageFinished(String url) {
        addLog("Page Loaded");
        btnConnect.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
