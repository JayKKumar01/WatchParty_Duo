package com.github.jaykkumar01.watchparty_duo.webfeed;

import android.webkit.JavascriptInterface;

import com.github.jaykkumar01.watchparty_duo.listeners.WebFeedListener;

public class WebBridge {
    private final WebFeedListener webFeedListener;

    public WebBridge(WebFeedListener webFeedListener) {
        this.webFeedListener = webFeedListener;
    }

    @JavascriptInterface
    public void onPeerOpen(String peerId) {
        webFeedListener.onPeerOpen(peerId);
    }

    @JavascriptInterface
    public void onConnectionOpen(String peerId, String remoteId) {
        webFeedListener.onConnectionOpen(peerId, remoteId);
    }

    @JavascriptInterface
    public void onBatchReceived(String jsonData) {
        webFeedListener.onBatchReceived(jsonData);
    }

    @JavascriptInterface
    public void onUpdate(String message){
        webFeedListener.onUpdate("From Javascript bridge: "+message);
    }

    @JavascriptInterface
    public void onConnectionClosed(){
        webFeedListener.onConnectionClosed();
    }
}
