package com.github.jaykkumar01.watchparty_duo.listeners;

public interface ImageFeedListener {
    void sendImageFeed(byte[] imageFeedBytes, long millis);
}
