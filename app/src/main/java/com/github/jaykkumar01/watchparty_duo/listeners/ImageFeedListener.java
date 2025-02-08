package com.github.jaykkumar01.watchparty_duo.listeners;

public interface ImageFeedListener {
    void onImageFeed(byte[] imageFeedBytes, long millis);

    void onError(String err);
    void onUpdate(String logMessage);
}
