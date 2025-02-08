package com.github.jaykkumar01.watchparty_duo.listeners;

public interface FeedListener {
    void onFeed(byte[] bytes, long millis, int feedType);

    void onError(String err);
    void onUpdate(String logMessage);
}
