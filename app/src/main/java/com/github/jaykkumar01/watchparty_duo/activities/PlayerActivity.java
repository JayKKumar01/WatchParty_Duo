package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.github.jaykkumar01.watchparty_duo.MainActivity;
import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.models.Peer;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PlayerActivity extends AppCompatActivity {


    private PlayerView playerView;
    private TextView userName;
    private ExoPlayer player;
    private ActivityResultLauncher<String> pickVideoLauncher;

    private Peer peer;
    private boolean isMute;

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



        initViews();

        // Retrieve extras from the intent
        Intent intent = getIntent();
        if (intent != null) {
            peer = (Peer) intent.getSerializableExtra(Constants.PEER);
            if (peer != null) {
                userName.setText(peer.getName());
            }
        }




        setupPickVideoLauncher();



    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        userName = findViewById(R.id.userName);
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
        ImageView imageView = (ImageView) view;
        isMute = !isMute;

        if (ConnectionService.getInstance() != null) {
            ConnectionService.getInstance().toggleMic(isMute);
        }

        imageView.setImageResource(isMute ? R.drawable.mic_off : R.drawable.mic_on);
    }


    public void endCall(View view) {
        if(ConnectionService.getInstance() != null){
            ConnectionService.getInstance().stop();
        }
        finish();

        startActivity(new Intent(this, MainActivity.class));
    }

    public void deafen(View view) {
    }

    public void sendMessage(View view) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
