package com.github.jaykkumar01.watchparty_duo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
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
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.github.jaykkumar01.watchparty_duo.webviewutils.Peer;
import com.github.jaykkumar01.watchparty_duo.webviewutils.PeerListener;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class PeerActivity extends AppCompatActivity implements PeerListener, FeedListener {

    private static final int TOTAL_PEERS = 1;
    private final Set<String> openedPeers = new HashSet<>();
    private final Set<String> connectedPeers = new HashSet<>();
    private final HashMap<String, String> connectionMap = new HashMap<>();
    private Peer[] peers;
    private long startTime;

    private ConstraintLayout layoutConnect, layoutJoin, imageFeedLayout;
    private ImageView peerFeed, remoteFeed;
    private TextInputEditText etJoinName, etCode;
    private TextView tvName;
    private AppCompatButton btnJoin, btnConnect;
    private String userName;

    private int sentCount = 0;
    private int receivedCount = 0;
    private int totalBytesPerSecond = 0;
    private static final int FPS = 10;
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_peer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        layoutConnect = findViewById(R.id.layoutConnect);
        layoutJoin = findViewById(R.id.layoutJoin);
        etJoinName = findViewById(R.id.etJoinName);
        etCode = findViewById(R.id.etCode);
        btnJoin = findViewById(R.id.btnJoin);
        btnConnect = findViewById(R.id.btnConnect);
        tvName = findViewById(R.id.tvName);
        imageFeedLayout = findViewById(R.id.imageFeedLayout);
        peerFeed = findViewById(R.id.peerFeedImageView);
        remoteFeed = findViewById(R.id.remoteFeedImageView);
    }

    public void connect(View view) {
        if (!PermissionHandler.hasPermission(this)) return;

        resetConnectionData();
        btnConnect.setText(R.string.connecting);
        btnConnect.setEnabled(false);

        userName = etJoinName.getText().toString().trim();
        if (userName.isEmpty()) {
            showToast("Please enter your name.");
            resetConnectButton();
            return;
        }

        int randomCode = Base.generateRandomCode(6);
        startTime = System.currentTimeMillis();

        peers = new Peer[TOTAL_PEERS];
        for (int i = 0; i < TOTAL_PEERS; i++) {
            peers[i] = new Peer(this, this);
            peers[i].initPeer(randomCode + "-" + i);
        }
    }

    public void join(View view) {
        btnJoin.setText(R.string.joining);
        btnJoin.setEnabled(false);

        String remoteId = etCode.getText().toString().trim();
        if (remoteId.isEmpty()) {
            showToast("Please enter Peer ID.");
            resetJoinButton();
            return;
        }

        startTime = System.currentTimeMillis();
        for (int i = 0; i < TOTAL_PEERS; i++) {
            peers[i].connect(remoteId + "-" + i);
        }

        resetJoinButton();
        layoutJoin.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPeerOpen(String peerId) {
        openedPeers.add(peerId);
        updateLogs("Peer Opened: " + peerId);

        if (openedPeers.size() == TOTAL_PEERS) {
            hideKeyboard();
            logElapsedTime("All Peers Opened Successfully!");
            tvName.setText("Welcome " + userName + ", Your ID: " + peerId);
            layoutConnect.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            resetConnectButton();
        }
    }

    @Override
    public void onConnectionOpen(String peerId, String remoteId) {
        connectedPeers.add(peerId);
        updateLogs("Peer: " + peerId + ", Remote: " + remoteId);
        connectionMap.put(peerId, remoteId);

        if (connectedPeers.size() == TOTAL_PEERS) {
            hideKeyboard();
            logElapsedTime("All Peers Connected Successfully!");
            layoutJoin.setVisibility(View.GONE);
            etCode.clearFocus();
            startLoggingImageUpdates();
        }
    }


    private void startLoggingImageUpdates() {
        updateLogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs("Updates: [" + sentCount + ", " + receivedCount + "], Size: "+(totalBytesPerSecond/1024.0) + " KB");

                // Reset sent and received counts
                sentCount = 0;
                receivedCount = 0;

                // Continue updating every second
                updateLogHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }





    private byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    @Override
    public void onReadImageFeed(String peerId, byte[] imageFeedBytes, long millis) {
        if (imageFeedBytes == null || imageFeedBytes.length == 0){
            return;
        }
        receivedCount++;
        totalBytesPerSecond += imageFeedBytes.length;
    }

    @Override
    public void onBatchReceived(String jsonDataBytes) {

    }


    @Override
    public void onFeed(byte[] bytes, long millis, int feedType) {
        peers[0].callJavaScript("onFeed", bytes, millis);
        sentCount++;
    }

    @Override
    public void onError(String err) {

    }

    @Override
    public void onUpdate(String logMessage) {

    }

    @SuppressLint("SetTextI18n")
    private void updateLogs(String message) {
        TextView logTextView = findViewById(R.id.logTextView);
        ScrollView logScrollView = findViewById(R.id.logScrollView);
        logTextView.append("\n" + message);
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void resetConnectionData() {
        openedPeers.clear();
        connectedPeers.clear();
        connectionMap.clear();
    }

    private void resetConnectButton() {
        btnConnect.setText(R.string.connect);
        btnConnect.setEnabled(true);
    }

    private void resetJoinButton() {
        btnJoin.setText(R.string.join);
        btnJoin.setEnabled(true);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logElapsedTime(String message) {
        updateLogs(message + " Time taken: " + (System.currentTimeMillis() - startTime) / 1000.0 + " sec");
    }

    // Method to hide keyboard
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
