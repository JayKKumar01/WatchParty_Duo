package com.github.jaykkumar01.watchparty_duo.playeractivityhelpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.gestures.MovementListener;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;

public class PlayerOrientationHandler {
    private final Activity activity;
    private final ConstraintLayout playerLayout;
    private final View optionLayout;
    private final View imageFeedLayout;
    private final ConstraintLayout smallRemoteFeedLayout;
    private final View LAYOUT1;

    private final TextureView remoteFeedTextureView,smallRemoteFeedTextureView,peerFeedTextureView;
    private final ConstraintLayout.LayoutParams playerLayoutLayoutParams;
    private final int marginInPx, paddingInPx;
    private final MovementListener movementListener;
    private ImageView fullScreen;
    private final ScrollView scrollView;
    private ExoPlayer player;

    @SuppressLint("ClickableViewAccessibility")
    public PlayerOrientationHandler(Activity activity, TextureView remoteFeedTextureView, TextureView peerFeedTextureView) {
        this.activity = activity;

        this.marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, activity.getResources().getDisplayMetrics());
        this.paddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, activity.getResources().getDisplayMetrics());
        this.remoteFeedTextureView = remoteFeedTextureView;
        this.peerFeedTextureView = peerFeedTextureView;
        this.playerLayout = activity.findViewById(R.id.playerLayout);
        playerLayoutLayoutParams = (ConstraintLayout.LayoutParams) playerLayout.getLayoutParams();
        this.optionLayout = activity.findViewById(R.id.optionLayout);
        this.imageFeedLayout = activity.findViewById(R.id.imageFeedLayout);
        this.smallRemoteFeedLayout = activity.findViewById(R.id.smallRemoteFeedLayout);
        this.LAYOUT1 = activity.findViewById(R.id.LAYOUT1);
        this.smallRemoteFeedTextureView = activity.findViewById(R.id.smallRemoteFeed);

        this.fullScreen = activity.findViewById(R.id.exo_screen);
        this.scrollView = activity.findViewById(R.id.scrollView);

        fullScreen.setOnClickListener(this::fullScreen);

        movementListener = new MovementListener(smallRemoteFeedLayout);
        smallRemoteFeedLayout.setOnTouchListener(movementListener);
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }
    public void fullScreen(View view) {
        if (player == null){
            return;
        }

        VideoSize videoSize = player.getVideoSize();
        boolean isNotLandscape = activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
        boolean isPortraitVideo = videoSize.height > videoSize.width;
        if ( isPortraitVideo && isNotLandscape){
            FeedService feedService = FeedService.getInstance();
            Integer currentDrawableId = (Integer) view.getTag();
            if (currentDrawableId != null && currentDrawableId == R.drawable.fullscreen) {
                fullScreen.setImageResource(R.drawable.fullscreen_exit);
                fullScreen.setTag(R.drawable.fullscreen_exit);
                if (feedService != null) {
                    feedService.setRemoteSurface(smallRemoteFeedTextureView);
                    feedService.setPeerSurface(null);
                }
                enableFullscreen();
                showPlayerOnly();

            }else {
                fullScreen.setImageResource(R.drawable.fullscreen);
                fullScreen.setTag(R.drawable.fullscreen);
                if (feedService != null) {
                    feedService.setFeedSurfaces(peerFeedTextureView,remoteFeedTextureView);
                }
                disableFullscreen();
                restoreDefaultLayout();
            }

            return;
        }
        if (isNotLandscape){

        }


        activity.setRequestedOrientation(
                isNotLandscape
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );
    }

    private String getFullRatio(){
        DisplayMetrics displayMatrix = activity.getResources().getDisplayMetrics();
        return displayMatrix.widthPixels + ":" + displayMatrix.heightPixels;
    }
    private String getWindowAspectRatio() {
        WindowManager windowManager = activity.getWindowManager();
        int width;
        int height;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            width = bounds.width();
            height = bounds.height();
        } else {
            // For API levels below 30
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }

        return width + ":" + height;
    }

    public void handleOrientationChange(int newOrientation) {
        // Connect feed surfaces to FeedService
        FeedService feedService = FeedService.getInstance();

        if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (feedService != null) {
                feedService.setRemoteSurface(smallRemoteFeedTextureView);
                feedService.setPeerSurface(null);
            }
            enableFullscreen();
            showPlayerOnly();
            fullScreen.setImageResource(R.drawable.fullscreen_exit);
            fullScreen.setTag(R.drawable.fullscreen_exit);
        } else {
            if (feedService != null) {
                feedService.setFeedSurfaces(peerFeedTextureView,remoteFeedTextureView);
            }
            disableFullscreen();
            restoreDefaultLayout();
            fullScreen.setImageResource(R.drawable.fullscreen);
            fullScreen.setTag(R.drawable.fullscreen);
        }

    }

    private void enableFullscreen() {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void disableFullscreen() {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void showPlayerOnly() {
        optionLayout.setVisibility(View.GONE);
        imageFeedLayout.setVisibility(View.GONE);
        LAYOUT1.setVisibility(View.GONE);
        smallRemoteFeedLayout.setVisibility(View.VISIBLE);
        setLayoutParams(true);
        movementListener.resetBoundaries();
    }

    private void restoreDefaultLayout() {
        smallRemoteFeedLayout.setVisibility(View.GONE);
        setLayoutParams(false);
        optionLayout.setVisibility(View.VISIBLE);
        imageFeedLayout.setVisibility(View.VISIBLE);
        LAYOUT1.setVisibility(View.VISIBLE);
    }

    private void setLayoutParams(boolean isLandscape) {
//        setScrollViewScrolling(scrollView,isLandscape);


        if (isLandscape) {
            playerLayoutLayoutParams.dimensionRatio = getWindowAspectRatio();
            playerLayoutLayoutParams.setMargins(0, 0, 0, 0);
            playerLayout.setPadding(0, 0, 0, 0);
            playerLayout.setBackgroundColor(ContextCompat.getColor(activity,R.color.theme_related));
        } else {
            playerLayoutLayoutParams.dimensionRatio = "16:9"; // Maintain aspect ratio
            playerLayoutLayoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            playerLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
            playerLayout.setBackgroundResource(R.drawable.video_player_feed_background);
        }

        playerLayout.setLayoutParams(playerLayoutLayoutParams);
    }
}
