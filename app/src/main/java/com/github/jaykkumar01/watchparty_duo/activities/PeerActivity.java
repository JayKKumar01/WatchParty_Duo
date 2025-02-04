package com.github.jaykkumar01.watchparty_duo.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import com.github.jaykkumar01.watchparty_duo.listeners.ImageFeedListener;
import com.github.jaykkumar01.watchparty_duo.transferfeeds.ImageFeed;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.github.jaykkumar01.watchparty_duo.webviewutils.Peer;
import com.github.jaykkumar01.watchparty_duo.webviewutils.PeerListener;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PeerActivity extends AppCompatActivity implements PeerListener, ImageFeedListener {

    private static final int TOTAL_PEERS = 1;
    private Set<String> openedPeers = new HashSet<>();
    private Set<String> connectedPeers = new HashSet<>();
    private HashMap<String,String> connectionMap = new HashMap<>();
    private Peer[] peers = new Peer[TOTAL_PEERS];
    private long startTime; // Variable to store the start time


    private ConstraintLayout layoutConnect,layoutJoin,imageFeedLayout;
    private ImageView peerFeed,remoteFeed;

    private TextInputEditText etJoinName,etCode;
    private TextView tvName;
    private AppCompatButton btnJoin,btnConnect;
    private String userName;
    private ImageFeed imageFeed;
    private final Random random = new Random();

    private int sentCount = 0;
    private int receivedCount = 0;
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());

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

        imageFeed = new ImageFeed(this,peerFeed);
        imageFeed.setImageFeedListener(this);
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
        if (!PermissionHandler.hasPermission(this)){
            return;
        }

        if (!openedPeers.isEmpty()){
            openedPeers.clear();
        }
        if (!connectedPeers.isEmpty()){
            connectedPeers.clear();
        }
        if (!connectionMap.isEmpty()){
            connectionMap.clear();
        }
        peers = new Peer[TOTAL_PEERS];

        btnConnect.setText("Connecting...");
        btnConnect.setEnabled(false);
        String name = etJoinName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            btnConnect.setText("Connect");
            btnConnect.setEnabled(true);
            return;
        }
        userName = name;

        int randomCode = Base.generateRandomCode(6);
        startTime = System.currentTimeMillis(); // Start tracking time

        // Initialize 10 peer instances
        for (int i = 0; i < TOTAL_PEERS; i++) {
            peers[i] = new Peer(this, this);
            peers[i].initPeer(randomCode + "-" + i);
        }
    }

    public void join(View view) {

        // Change button text to "Connecting..." and disable it
        btnJoin.setText("Joining...");
        btnJoin.setEnabled(false);
        String remoteId = etCode.getText().toString().trim();

        // Check if the Name field is empty
        if (remoteId.isEmpty()) {
            // Display an error message if the name is empty
            Toast.makeText(this, "Please enter Peer ID", Toast.LENGTH_SHORT).show();
            // Revert button text to "Join" and enable it
            btnJoin.setText("Join");
            btnJoin.setEnabled(true);
            return; // Exit the method if the name is empty
        }

        startTime = System.currentTimeMillis();
        // Initialize 10 peer instances
        for (int i = 0; i < TOTAL_PEERS; i++) {
            peers[i].connect(remoteId + "-" + i);
        }

        btnJoin.setText("Join");
        etCode.setText("");
        btnJoin.setEnabled(true);
        layoutJoin.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPeerOpen(String peerId) {




        openedPeers.add(peerId);
        updateLogs("Peer Opened: " + peerId);

        // Check if all 10 peers are open
        if (openedPeers.size() == TOTAL_PEERS) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            double elapsedSeconds = elapsedTime / 1000.0; // Convert ms to seconds
            updateLogs("All Peers Opened Successfully! Time taken: " + elapsedSeconds + " sec");

            tvName.setText("Welcome "+ userName+ ", Your ID: "+ peerId);
            layoutConnect.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            btnConnect.setText("Connect");
            btnConnect.setEnabled(true);

            etJoinName.clearFocus();
        }
    }

    @Override
    public void onConnectionOpen(String peerId, String remoteId) {
        connectedPeers.add(peerId);
        updateLogs("Peer: " + peerId + ", Remote: "+remoteId);
        connectionMap.put(peerId,remoteId);

        // Check if all 10 peers are open
        if (connectedPeers.size() == TOTAL_PEERS) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            double elapsedSeconds = elapsedTime / 1000.0; // Convert ms to seconds
            updateLogs("All Peers Connected Successfully! Time taken: " + elapsedSeconds + " sec");
            layoutJoin.setVisibility(View.GONE);
            etCode.clearFocus();

            imageFeed.openCamera();
            imageFeedLayout.setVisibility(View.VISIBLE);
            // Start logging updates per second
            startLoggingImageUpdates();
        }
    }

    private void startLoggingImageUpdates() {
        updateLogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs("Updates: [" + sentCount+","+receivedCount+"]");
                receivedCount = 0; // Reset count after logging
                sentCount = 0;
                updateLogHandler.postDelayed(this, 1000); // Schedule next log after 1 second
            }
        }, 1000);
    }

    @Override
    public void onReadImageFeed(String peerId, byte[] imageFeedBytes, long millis) {
        receivedCount++; // Increment update count on each image received
        //remoteFeed.setImageBitmap(BitmapUtils.getBitmap(imageFeedBytes));
    }



    @Override
    public void sendImageFeed(byte[] imageFeedBytes, long millis) {
//        updateLogs("["+millis+"] Feed: "+imageFeedBytes.length);

        // Choose a random peer index
        int randomIndex = random.nextInt(TOTAL_PEERS);

        // Ensure the selected peer is not null before calling the method
        if (peers[randomIndex] != null) {
            peers[randomIndex].callJavaScript("sendImageFeed", imageFeedBytes, millis);
            sentCount++;
        } else {
            updateLogs("Selected Peer is null, retrying...");
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateLogs(String message){
        TextView logTextView = findViewById(R.id.logTextView);
        ScrollView logScrollView = findViewById(R.id.logScrollView);

        // Prepend new message at the top
        String currentText = logTextView.getText().toString();
        logTextView.setText(currentText + "\n" + message);

        // Auto-scroll to top
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
    }
}
