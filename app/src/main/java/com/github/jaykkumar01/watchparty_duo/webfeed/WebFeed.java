package com.github.jaykkumar01.watchparty_duo.webfeed;

import android.content.Context;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.listeners.WebFeedListener;

public class WebFeed{
    private final WebFeedListener webFeedListener;
    private final WebFeedHelper helper;

    private boolean isPeerOpen;
    private boolean isConnectionOpen;
    private boolean isConnectionAlive = true;


    public WebFeed(Context context, WebFeedListener webFeedListener) {
        helper = new WebFeedHelper(context);
        this.webFeedListener = webFeedListener;
    }

    public void start(){
        helper.initWebView(webFeedListener);
    }

    public void connect(String remoteId) {
        helper.connect(remoteId);
    }

    public void stop() {
        isPeerOpen = false;
        isConnectionOpen = false;
        helper.destroy(isConnectionAlive);
    }

    public void onPeerOpen(String peerId) {
        isPeerOpen = true;
    }

    public void onConnectionOpen(String peerId, String remoteId) {
        isConnectionOpen = true;
    }

    public WebView getWebView() {
        return helper.getWebView();
    }

    public void playbackToRemote(int action, Object object) {
        helper.playbackToRemote(action,object);
    }

    public void onConnectionAlive(boolean isConnectionAlive) {
        this.isConnectionAlive = isConnectionAlive;
    }
}
