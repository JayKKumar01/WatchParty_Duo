package com.github.jaykkumar01.watchparty_duo.webfeed;

public interface WebFeedListener {
    void onPeerOpen(String peerId);
    void onConnectionOpen(String peerId, String remoteId);
    void onBatchReceived(String jsonData);
}
