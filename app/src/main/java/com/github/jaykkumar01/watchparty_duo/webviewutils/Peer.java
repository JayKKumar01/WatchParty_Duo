package com.github.jaykkumar01.watchparty_duo.webviewutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.utils.ObjectUtil;

public class Peer {
    private final Context context;
    private final PeerListener peerListener;
    private final Handler mainHandler;
    private WebView webView;
    private boolean isPeerOpen;
    private boolean isConnectionOpen;

    private String peerId;

    public Peer(Context context, PeerListener peerListener) {
        this.context = context;
        this.peerListener = peerListener;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void initPeer(String peerId){
        if (this.peerId != null){
            return;
        }
        this.peerId = peerId;
        mainHandler.post(this::setupWebView);
    }
    public void initPeer(){
        if (this.peerId != null){
            return;
        }
        peerId = "";
        mainHandler.post(this::setupWebView);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setupWebView() {
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
                if (peerId.isEmpty()){
                    callJavaScript("initPeer");
                }else {
                    callJavaScript("initPeer",peerId);
                }
            }
        });

        webView.addJavascriptInterface(new JavaScriptInterface(context,this,peerListener,mainHandler), "Android");

        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String path = "file:android_asset/index.html";
        webView.loadUrl(path);
    }

    public void connect(String otherPeerId) {
        callJavaScript("connect", otherPeerId);
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
        if (webView != null) {
            mainHandler.post(() -> webView.loadUrl(javascriptCommand));
        }
    }

    public void stop() {
        mainHandler.post(() -> {
            if (webView != null) {
                webView.loadUrl("");
                webView = null;
            }
        });
    }

    public boolean isPeerOpen() {
        return isPeerOpen;
    }

    public boolean isConnectionOpen() {
        return isConnectionOpen;
    }

    public void setConnectionOpen(boolean connectionOpen) {
        isConnectionOpen = connectionOpen;
    }

    public void setPeerOpen(boolean peerOpen) {
        isPeerOpen = peerOpen;
    }


}
