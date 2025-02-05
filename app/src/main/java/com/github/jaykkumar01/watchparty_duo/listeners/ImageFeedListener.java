package com.github.jaykkumar01.watchparty_duo.listeners;

public interface ImageFeedListener {
    void onImageFeed(byte[] imageFeedBytes, long millis);
}
