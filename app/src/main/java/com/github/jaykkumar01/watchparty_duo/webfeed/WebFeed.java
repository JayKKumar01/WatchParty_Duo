package com.github.jaykkumar01.watchparty_duo.webfeed;

import android.content.Context;

import com.github.jaykkumar01.watchparty_duo.services.FeedService;

public class WebFeed implements WebFeedListener{
    private final WebFeedHelper helper;

    private boolean isPeerOpen;
    private boolean isConnectionOpen;

    public WebFeed(Context context) {
        helper = new WebFeedHelper(context);
    }

    public void start(){
        helper.initWebView(this);
    }

    public void connect(String remoteId) {
        helper.connect(remoteId);
    }

    public void stop() {
        isPeerOpen = false;
        isConnectionOpen = false;
        helper.destroy();
    }

    @Override
    public void onPeerOpen(String peerId) {
        isPeerOpen = true;
        helper.onPeerOpen(peerId);
    }

    @Override
    public void onConnectionOpen(String peerId, String remoteId) {
        isConnectionOpen = true;
        if (FeedService.getInstance() != null) {
            FeedService.getInstance().updateNotification(true);
        }
        helper.onConnectionOpen(peerId,remoteId);
    }

    @Override
    public void onBatchReceived(String jsonData) {

    }

    public boolean isPeerOpen(){
        return isPeerOpen;
    }
    public boolean isConnectionOpen() {
        return isConnectionOpen;
    }
}
