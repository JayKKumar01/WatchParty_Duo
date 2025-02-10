package com.github.jaykkumar01.watchparty_duo.managers;

import android.content.Context;

import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.services.FeedService;
import com.github.jaykkumar01.watchparty_duo.webfeed.WebFeed;

public class FeedManager implements FeedListener {
    private final WebFeed webFeed;
    private final AudioFeed audioFeed;



    public FeedManager(Context context) {
        this.webFeed = new WebFeed(context);
        this.audioFeed = new AudioFeed(context,this);
    }



    public void startWebFeed(){
        webFeed.start();
    }

    public void connect(String remoteId) {
        webFeed.connect(remoteId);
    }


    public void startFeeds() {
//        webFeed.start();
        audioFeed.start();
    }

    public void stopFeeds() {
        webFeed.stop();
        audioFeed.stop();
    }

    @Override
    public void onFeed(byte[] bytes, long millis, int feedType) {

    }

    @Override
    public void onError(String err) {

    }

    @Override
    public void onUpdate(String logMessage) {

    }
}
