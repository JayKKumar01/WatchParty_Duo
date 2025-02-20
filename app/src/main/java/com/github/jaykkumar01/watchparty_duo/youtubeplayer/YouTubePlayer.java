package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.jaykkumar01.watchparty_duo.managers.YouTubePlayerManager;

import org.json.JSONObject;

public class YouTubePlayer {
    private final Activity activity;
    private final WebView webView;
    private final ConstraintLayout playerLayout;
    private final YouTubePlayerHandler handler;
    private final YouTubePlayerManager manager; // ✅ Manager instance

    public YouTubePlayer(YouTubePlayerHandler handler, Activity activity, YouTubePlayerManager manager, WebView webView) {
        this.handler = handler;
        this.activity = activity;
        this.manager = manager;
        this.webView = webView;
        this.playerLayout = (ConstraintLayout) webView.getParent();

        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new MyChrome());
        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(new JavaScriptBridge(), "Android");
        webView.loadUrl("file:///android_asset/youtube/index.html");
    }

    public void fetchVideoTitle(String videoId) {
        webView.loadUrl("javascript:fetchVideoTitle('" + videoId + "')");
    }
    public void loadVideo(String videoId, int autoplay) {
        webView.loadUrl("javascript:loadVideo('" + videoId + "', " + autoplay + ")");
    }
    public void loadVideo(String videoId, int autoplay, int startTime) {
        webView.loadUrl("javascript:loadVideo('" + videoId + "', " + autoplay + ", " + startTime + ")");
    }



    public void stop() {
        webView.loadUrl("javascript:stopVideo()");
    }

    public void requestPlayback() {
        webView.loadUrl("javascript:requestPlayback()");
    }

    public void updatePlayback(boolean isPlaying, int currentPosition) {
        webView.loadUrl("javascript:updatePlayback(" + isPlaying + ", " + currentPosition + ")");
    }


    private class JavaScriptBridge {
        @JavascriptInterface
        public void onIFrameAPIReady() {
            handler.onIFrameAPIReady();
        }

        @JavascriptInterface
        public void onPlayerCreated(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String title = jsonObject.getString("title");
                int duration = jsonObject.getInt("duration");

                activity.runOnUiThread(() -> handler.onPlayerCreated(title,duration));

                Log.d("YouTube", "Video Title: " + title + ", Duration: " + duration);

                // Send the extracted data to the callback
//                callback.onVideoInfoReceived(title, duration);
            } catch (Exception e) {
                Log.e("YouTube", "Error parsing video info JSON", e);
            }


        }

        @JavascriptInterface
        public void onPlayerReady(){
//            handler.onPlayerReady();
            manager.setPlayer(YouTubePlayer.this);
        }



        @JavascriptInterface
        public void onPlay(int timeMs) {
            manager.onPlay(timeMs);
        }

        @JavascriptInterface
        public void onPause(int timeMs) {
            manager.onPause(timeMs);
        }

        @JavascriptInterface
        public void onSeek(int timeMs) {
            manager.onSeek(timeMs);
        }

        // ✅ New JavaScript callback to receive updates before destroying the player
        @JavascriptInterface
        public void onDestroy(int lastPosition) {
            handler.onLastPosition(lastPosition);
            manager.resetPlayer(lastPosition);
        }
        @JavascriptInterface
        public void onRequestPlayback(boolean isPlaying, int lastPosition) {
            manager.playbackToRemote(isPlaying, lastPosition);
        }

    }

    private class MyChrome extends WebChromeClient {
        View screen = null;

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            request.grant(request.getResources());
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    screen = view;
                    playerLayout.removeView(webView);
                    addView(screen);
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            });
        }

        @Override
        public void onHideCustomView() {
            activity.runOnUiThread(new Runnable() {
                @SuppressLint("SourceLockedOrientationActivity")
                @Override
                public void run() {
                    playerLayout.removeView(screen);
                    screen = null;
                    addView(webView);
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            });

        }

    }

    public void addView(View screen) {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );
        screen.setLayoutParams(layoutParams);
        playerLayout.addView(screen);
    }
}
