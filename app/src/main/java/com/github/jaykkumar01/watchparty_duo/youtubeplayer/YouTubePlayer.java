package com.github.jaykkumar01.watchparty_duo.youtubeplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.jaykkumar01.watchparty_duo.R;

public class YouTubePlayer {
    private final Activity activity;
    private final WebView webView;
    private final YouTubePlayerHandler handler;
    private final YouTubePlayerManager manager; // ✅ Manager instance

    public YouTubePlayer(YouTubePlayerHandler handler, Activity activity, YouTubePlayerManager manager) {
        this.handler = handler;
        this.activity = activity;
        this.manager = manager;
        this.webView = activity.findViewById(R.id.webViewYouTube);

        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(activity, "page loaded!", Toast.LENGTH_SHORT).show();
            }
        });

        webView.addJavascriptInterface(new JavaScriptBridge(), "Android");
        webView.loadUrl("file:///android_asset/youtube/index.html");
    }

    public void loadVideo(String videoId) {
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl("javascript:loadVideo('" + videoId + "')");
    }

    public void stop() {
        webView.loadUrl("javascript:stopVideo()");
    }

    private class JavaScriptBridge {
        @JavascriptInterface
        public void onReady() {
            handler.onPlayerReady();
        }

        @JavascriptInterface
        public void onPlayerReady(){

        }

        @JavascriptInterface
        public void onPlay(long timeMs) {
            manager.onPlay(timeMs);
        }

        @JavascriptInterface
        public void onPause(long timeMs) {
            manager.onPause(timeMs);
        }

        @JavascriptInterface
        public void onSeek(long timeMs) {
            manager.onSeek(timeMs);
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
                    Toast.makeText(activity, "FullScreen Entered", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onHideCustomView() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Fullscreen Exited!", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public void addView(View screen, ConstraintLayout playerLayout) {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );
        screen.setLayoutParams(layoutParams);
        playerLayout.addView(screen);
    }
}
