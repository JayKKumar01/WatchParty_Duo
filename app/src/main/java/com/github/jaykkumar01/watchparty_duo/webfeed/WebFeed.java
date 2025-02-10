package com.github.jaykkumar01.watchparty_duo.webfeed;

import android.content.Context;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;

public class WebFeed{
    private final WebFeedListener webFeedListener;
    private final WebFeedHelper helper;

    private boolean isPeerOpen;
    private boolean isConnectionOpen;

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
        helper.destroy();
    }

    public void onPeerOpen(String peerId) {
        isPeerOpen = true;
        helper.onPeerOpen(peerId);
    }

    public void onConnectionOpen(String peerId, String remoteId) {
        isConnectionOpen = true;
        helper.onConnectionOpen(peerId,remoteId);
    }


    public WebView getWebView() {
        return helper.getWebView();
    }
}
