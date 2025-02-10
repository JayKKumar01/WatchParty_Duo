package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
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

import com.github.jaykkumar01.watchparty_duo.MainActivity;
import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.models.PeerModel;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;

public class PlayerActivity extends AppCompatActivity {

    private static PlayerActivity instance;

    public static PlayerActivity getInstance(){
        return instance;
    }


    private PlayerView playerView;
    private TextView userName;
    private ExoPlayer player;
    private ActivityResultLauncher<String> pickVideoLauncher;

    private PeerModel peerModel;
    private ImageView peerFeedImageView,remoteFeedImageView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for the main thread

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
        instance = this;

        initViews();

        // Retrieve extras from the intent
        Intent intent = getIntent();
        if (intent != null) {
            peerModel = (PeerModel) intent.getSerializableExtra(Constants.PEER);
            if (peerModel != null) {
                userName.setText(peerModel.getName());
            }
        }



        setupPickVideoLauncher();

    }




    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        userName = findViewById(R.id.userName);
        peerFeedImageView = findViewById(R.id.peerFeedImageView);
        remoteFeedImageView = findViewById(R.id.remoteFeedImageView);
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
//        ImageView imageView = (ImageView) view;
//        AppData.getInstance().setMute(!AppData.getInstance().isMute());
//
//        if (ConnectionService.getInstance() != null) {
//            ConnectionService.getInstance().toggleMic();
//        }
//
//        imageView.setImageResource(AppData.getInstance().isMute() ? R.drawable.mic_off : R.drawable.mic_on);
    }


    public void endCall(View view) {
        AppData.getInstance().reset();
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
