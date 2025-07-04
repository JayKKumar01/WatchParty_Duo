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
    public void onMetaData(String jsonData){
        webFeedListener.onMetaData(jsonData);
    }

    @JavascriptInterface
    public void onConnectionOpen(String peerId, String remoteId,int count) {
        webFeedListener.onConnectionOpen(peerId, remoteId,count);
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

    @JavascriptInterface
    public void onConnectionAlive(boolean isConnectionAlive){
        webFeedListener.onConnectionAlive(isConnectionAlive);
    }

    @JavascriptInterface
    public void onPeerDisconnected(String peerId){

    }
    @JavascriptInterface
    public void onPeerRestart(){
        webFeedListener.onPeerRestart();
    }

    @JavascriptInterface
    public void onPeerError(){
        webFeedListener.onPeerError();
    }
    @JavascriptInterface
    public void onRestartConnection(){
        webFeedListener.onRestartConnection();
    }

    @JavascriptInterface
    public void onPeerRetryLimitReached(){
        webFeedListener.onPeerRetryLimitReached();
    }

    @JavascriptInterface
    public void onPlaybackUpdate(String jsonData){
        webFeedListener.onPlaybackUpdate(jsonData);
    }


}
