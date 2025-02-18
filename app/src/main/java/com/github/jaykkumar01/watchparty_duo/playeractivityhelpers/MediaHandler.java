package com.github.jaykkumar01.watchparty_duo.playeractivityhelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.exoplayer.ExoPlayerHandler;
import com.github.jaykkumar01.watchparty_duo.helpers.YouTubeIDExtractor;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.youtubeplayer.YouTubePlayerHandler;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class MediaHandler {
    private final AppCompatActivity activity;
    private final ExoPlayerHandler exoPlayerHandler;
    private final YouTubePlayerHandler youtubePlayerHandler;
    private final ActivityResultLauncher<Intent> videoPickerLauncher;

    private TextView currentMediaTxt;
    private TextView playOfflineVideo;
    private Uri currentVideoUri;

    private Drawable defaultBackground;
    private int defaultTextColor;
    private Drawable clickedBackground;
    private int clickedTextColor;


    public MediaHandler(AppCompatActivity activity, ExoPlayerHandler exoPlayerHandler, YouTubePlayerHandler youtubePlayerHandler){
        this.activity = activity;
        this.exoPlayerHandler = exoPlayerHandler;
        this.youtubePlayerHandler = youtubePlayerHandler;

        initializeUIComponents();


        videoPickerLauncher = registerVideoPickerLauncher();
    }

    private ActivityResultLauncher<Intent> registerVideoPickerLauncher() {
        return  activity.registerForActivityResult(
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

    private void initializeUIComponents() {
        // Initialize UI components and set click listeners
        defaultBackground = ContextCompat.getDrawable(activity, R.drawable.video_player_feed_background);
        defaultTextColor = ContextCompat.getColor(activity, R.color.white);
        clickedBackground = ContextCompat.getDrawable(activity, R.drawable.big_button_bg);
        clickedTextColor = ContextCompat.getColor(activity, R.color.theme_color);

        ImageView imgAddMedia = activity.findViewById(R.id.imgAddMedia);
        playOfflineVideo = activity.findViewById(R.id.playOfflineVideo);
        currentMediaTxt = activity.findViewById(R.id.currentMediaTxt);
        ImageView btnRefresh = activity.findViewById(R.id.btnRefresh);


        // Initialize layout components
        ConstraintLayout expPlayerLayout = activity.findViewById(R.id.exoplayerLayout);
        ConstraintLayout youtubePlayerLayout = activity.findViewById(R.id.youtubePlayerLayout);

        // Set listeners for buttons and text views
        imgAddMedia.setOnClickListener(view -> handleAddMediaClick());
        playOfflineVideo.setOnClickListener(view -> startSyncPlay());
        btnRefresh.setOnClickListener(view -> handleRefreshClick());
        initializeArchiveCelestialButtons(expPlayerLayout, youtubePlayerLayout);




    }

    private void initializeArchiveCelestialButtons(ConstraintLayout expPlayerLayout, ConstraintLayout youtubePlayerLayout) {
        TextView tvArchive = activity.findViewById(R.id.tvArchive);
        TextView tvCelestial = activity.findViewById(R.id.tvCelestial);

        tvArchive.setOnClickListener(view -> handleArchiveCelestialClick(tvArchive, tvCelestial, expPlayerLayout, youtubePlayerLayout));
        tvCelestial.setOnClickListener(view -> handleArchiveCelestialClick(tvCelestial, tvArchive, youtubePlayerLayout, expPlayerLayout));
    }

    private void handleArchiveCelestialClick(TextView activeTextView, TextView inactiveTextView,
                                             ConstraintLayout activeLayout, ConstraintLayout inactiveLayout) {
        handleRefreshClick();
        setActiveTextViewStyle(activeTextView);
        resetInactiveTextViewStyle(inactiveTextView);
        toggleLayoutVisibility(activeLayout, inactiveLayout);
    }

    private void setActiveTextViewStyle(TextView textView) {
        textView.setBackground(clickedBackground);
        textView.setTextColor(clickedTextColor);
    }

    private void resetInactiveTextViewStyle(TextView textView) {
        textView.setBackground(defaultBackground);
        textView.setTextColor(defaultTextColor);
    }

    private void toggleLayoutVisibility(ConstraintLayout activeLayout, ConstraintLayout inactiveLayout) {
        activeLayout.setVisibility(View.VISIBLE);
        inactiveLayout.setVisibility(View.GONE);
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

    private void handleAddMediaClick() {
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
    private void handleRefreshClick(){
        resetCurrentMedia();
        exoPlayerHandler.resetPlayer();
        youtubePlayerHandler.resetPlayer();

    }

    private void updateCurrentMedia(String mediaName) {
        if (mediaName == null || mediaName.isEmpty()) {
            return;
        }
        currentMediaTxt.setText(mediaName);
        currentMediaTxt.setVisibility(View.VISIBLE);
        playOfflineVideo.setVisibility(View.VISIBLE);
    }

    private void resetCurrentMedia(){
        currentVideoUri = null;
        currentMediaTxt.setVisibility(View.GONE);
        playOfflineVideo.setVisibility(View.GONE);
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

