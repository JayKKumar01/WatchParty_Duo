package com.github.jaykkumar01.watchparty_duo.playeractivityhelpers;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.exoplayer.ExoPlayerHandler;

public class MediaHandler {

    private final Context context;
    private final TextView currentMediaTxt;
    private final ActivityResultLauncher<String> pickVideoLauncher;
    private final TextView playOfflineVideo;
    private final ExoPlayerHandler exoPlayerHandler;
    private final ImageView btnRefresh;
    private Uri currentVideoUri = null;

    public MediaHandler(AppCompatActivity activity, ExoPlayerHandler exoPlayerHandler) {
        this.context = activity;
        this.exoPlayerHandler = exoPlayerHandler;
        // Find views
        ImageView imgAddMedia = activity.findViewById(R.id.imgAddMedia);
        playOfflineVideo = activity.findViewById(R.id.playOfflineVideo);
        currentMediaTxt = activity.findViewById(R.id.currentMediaTxt);
        this.btnRefresh = activity.findViewById(R.id.btnRefresh);

        // Set click listeners
        imgAddMedia.setOnClickListener(view -> selectVideo());
        playOfflineVideo.setOnClickListener(view -> startSyncPlay());
        btnRefresh.setOnClickListener(view -> resetPlayerLayout());


        pickVideoLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onResult
        );
    }

    private void onResult(Uri selectedVideoUri) {
        if (selectedVideoUri == null){
            Toast.makeText(context, "Couldn't get video", Toast.LENGTH_SHORT).show();
            return;
        }
        this.currentVideoUri = selectedVideoUri;
        String fileName = getFileName(context, selectedVideoUri);
        updateCurrentMedia(fileName);
    }

    private void selectVideo() {
        pickVideoLauncher.launch("video/*");
    }

    private void startSyncPlay() {
        exoPlayerHandler.playMedia(currentVideoUri);
    }
    private void resetPlayerLayout(){
        exoPlayerHandler.resetPlayer(true);
    }

    private void updateCurrentMedia(String mediaName) {
        if (mediaName != null && !mediaName.isEmpty()) {
            currentMediaTxt.setText(mediaName);
            currentMediaTxt.setVisibility(View.VISIBLE);
            playOfflineVideo.setVisibility(View.VISIBLE);
        } else {
            currentMediaTxt.setVisibility(View.GONE);
            playOfflineVideo.setVisibility(View.GONE);
        }
    }

    @Nullable
    private String getFileName(Context context, Uri uri) {
        String fileName = "Unknown";
        try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }
        return fileName;
    }
}

