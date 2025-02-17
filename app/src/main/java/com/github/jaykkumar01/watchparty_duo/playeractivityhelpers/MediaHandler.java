package com.github.jaykkumar01.watchparty_duo.playeractivityhelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.github.jaykkumar01.watchparty_duo.interfaces.PermissionCodes;

public class MediaHandler {
    private final Activity activity;
    private final TextView currentMediaTxt;
    private final TextView playOfflineVideo;
    private final ExoPlayerHandler exoPlayerHandler;
    private final ActivityResultLauncher<Intent> videoPickerLauncher;

    private Uri currentVideoUri = null;

    public MediaHandler(AppCompatActivity activity, ExoPlayerHandler exoPlayerHandler){
        this.activity = activity;
        this.exoPlayerHandler = exoPlayerHandler;
        // Find views
        ImageView imgAddMedia = activity.findViewById(R.id.imgAddMedia);
        playOfflineVideo = activity.findViewById(R.id.playOfflineVideo);
        currentMediaTxt = activity.findViewById(R.id.currentMediaTxt);
        ImageView btnRefresh = activity.findViewById(R.id.btnRefresh);

        // Set click listeners
        imgAddMedia.setOnClickListener(view -> selectVideo());
        playOfflineVideo.setOnClickListener(view -> startSyncPlay());
        btnRefresh.setOnClickListener(view -> resetPlayerLayout());

        videoPickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedVideoUri = result.getData().getData();
                        onResult(selectedVideoUri);
                    } else {
                        Toast.makeText(activity, "Video selection canceled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void onResult(Uri selectedVideoUri) {
        if (selectedVideoUri == null){
            Toast.makeText(activity, "Couldn't get video", Toast.LENGTH_SHORT).show();
            return;
        }
        this.currentVideoUri = selectedVideoUri;
        String fileName = getFileName(activity, selectedVideoUri);
        updateCurrentMedia(fileName);
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        videoPickerLauncher.launch(intent);
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

