package com.github.jaykkumar01.watchparty_duo;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.activities.CameraActivity;
import com.github.jaykkumar01.watchparty_duo.activities.PlayerActivity;
import com.github.jaykkumar01.watchparty_duo.models.Peer;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.google.android.material.textfield.TextInputEditText;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity{

    private ConstraintLayout layoutConnect,layoutJoin;

    private TextInputEditText etJoinName,etCode;
    private TextView tvName;
    private AppCompatButton btnJoin,btnConnect;
    
    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private Peer peer;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        instance = this;
        initViews();
        createNotificationChannel();
    }

    private void initViews() {
        layoutConnect = findViewById(R.id.layoutConnect);
        layoutJoin = findViewById(R.id.layoutJoin);
        etJoinName = findViewById(R.id.etJoinName);
        etCode = findViewById(R.id.etCode);
        btnJoin = findViewById(R.id.btnJoin);
        btnConnect = findViewById(R.id.btnConnect);
        tvName = findViewById(R.id.tvName);
    }

    public void connect(View view) {
        if (!PermissionHandler.hasPermission(this)){
            Toast.makeText(instance, "Please enable All Permissions!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Change button text to "Connecting..." and disable it
        btnConnect.setText("Connecting...");
        btnConnect.setEnabled(false);
        // Get the text input from the Name and Room Code fields
        String name = etJoinName.getText().toString().trim();
        // Check if the Name field is empty
        if (name.isEmpty()) {
            // Display an error message if the name is empty
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            // Revert button text to "Join" and enable it
            btnConnect.setText("Connect");
            btnConnect.setEnabled(true);
            return; // Exit the method if the name is empty
        }
        peer = new Peer(name);
        // Proceed with the connection process if the name is valid
        startForegroundService();
    }


    public void onPeerOpen(String peerId){
        runOnUiThread(() -> {
            peer.setPeerId(peerId);
            tvName.setText("Welcome "+peer.getName()+ ", Your ID: "+ peerId);
            layoutConnect.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            btnConnect.setText("Connect");
            btnConnect.setEnabled(true);
        });

    }
    public void onConnectionOpen(String remoteId){
        Toast.makeText(this, "Connected with: "+remoteId, Toast.LENGTH_SHORT).show();
        runOnUiThread(() -> {
            if (ConnectionService.getInstance() != null) {
                ConnectionService.getInstance().startAudioTransfer();
            }
            peer.setRemoteId(remoteId);
            btnJoin.setText("Join");
            btnJoin.setEnabled(true);
            launchPlayerActivity();
        });


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
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().connect(remoteId);
            // Start a timer for 5 seconds to check connection status
            new android.os.Handler().postDelayed(() -> {
                if (!AppData.getInstance().isConnectionEstablished()) { // If not connected, reset join button
                    btnJoin.setText("Join");
                    btnJoin.setEnabled(true);
                    Toast.makeText(this, "Connection failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }, 5000);
        }
    }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, ConnectionService.class);
        serviceIntent.putExtra(Constants.PEER,peer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(Constants.CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void launchPlayerActivity(){
        Intent intent = new Intent(this, PlayerActivity.class);

        // Add extras to the intent
        intent.putExtra(Constants.PEER, peer);
        finish(); // Destroy the current activity before launching the new one
        startActivity(intent);
    }


    public void openCameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        finish(); // Destroy the current activity before launching the new one
        startActivity(intent);
    }
}