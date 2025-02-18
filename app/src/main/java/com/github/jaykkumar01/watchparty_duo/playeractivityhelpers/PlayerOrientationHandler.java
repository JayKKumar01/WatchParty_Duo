package com.github.jaykkumar01.watchparty_duo.playeractivityhelpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.github.jaykkumar01.watchparty_duo.R;
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
    private ImageView fullScreen;
    private final ScrollView scrollView;

    public PlayerOrientationHandler(Activity activity, TextureView remoteFeedTextureView,TextureView peerFeedTextureView) {
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
    }

    private String getFullRatio(){
        DisplayMetrics displayMatrix = activity.getResources().getDisplayMetrics();
        return displayMatrix.widthPixels + ":" + displayMatrix.heightPixels;
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setScrollViewScrolling(ScrollView scrollView, boolean disable) {
        if (disable) {
            scrollView.setOnTouchListener((v, event) -> true); // Disable scrolling
        } else {
            scrollView.setOnTouchListener(null); // Enable scrolling by removing listener
        }
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
        } else {
            if (feedService != null) {
                feedService.setFeedSurfaces(peerFeedTextureView,remoteFeedTextureView);
            }
            disableFullscreen();
            restoreDefaultLayout();
            fullScreen.setImageResource(R.drawable.fullscreen);
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
    }

    private void restoreDefaultLayout() {
        smallRemoteFeedLayout.setVisibility(View.GONE);
        setLayoutParams(false);
        optionLayout.setVisibility(View.VISIBLE);
        imageFeedLayout.setVisibility(View.VISIBLE);
        LAYOUT1.setVisibility(View.VISIBLE);
    }

    private void setLayoutParams(boolean isLandscape) {
        setScrollViewScrolling(scrollView,isLandscape);

        if (isLandscape) {
            playerLayoutLayoutParams.dimensionRatio = getFullRatio();
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
