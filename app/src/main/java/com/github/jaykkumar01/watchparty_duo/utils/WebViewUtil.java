package com.github.jaykkumar01.watchparty_duo.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.jaykkumar01.watchparty_duo.interfaces.JavaScriptInterface;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;

public class WebViewUtil {
    private final Context context;
    private WebView webView;
    private final Handler mainHandler;
    public WebViewUtil(Context context){
        this.context = context;
        mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(this::setup);
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void setup() {
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                callJavaScript("handlePeer");
            }

        });
        JavaScriptInterface javascriptInterface = new JavaScriptInterface(context);
        webView.addJavascriptInterface(javascriptInterface, "Android");
//        webView.addJavascriptInterface(new ExoPlayerBridge(CallService.this),"ExoPlayer");
//        webView.addJavascriptInterface(new YouTubePlayerBridge(CallService.this),"YouTubePlayer");

        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String path = "file:android_asset/index.html";
        webView.loadUrl(path);
    }

    public void callJavaScript(String func, Object... args) {
        StringBuilder argString = new StringBuilder();
        if (args.length > 0) {
            if (args[0] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[0]));
            } else if (args[0] instanceof byte[]) {
                argString.append(ObjectUtil.preserveBytes((byte[]) args[0]));
            } else {
                argString.append(args[0]);
            }
        }
        for (int i = 1; i < args.length; i++) {
            argString.append(",");
            if (args[i] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[i]));
            } else if (args[i] instanceof byte[]) {
                argString.append(ObjectUtil.preserveBytes((byte[]) args[i]));
            } else {
                argString.append(args[i]);
            }
        }
        final String javascriptCommand = String.format("javascript:%s(%s)", func, argString.toString());
        mainHandler.post(() -> webView.loadUrl(javascriptCommand));
    }
}
