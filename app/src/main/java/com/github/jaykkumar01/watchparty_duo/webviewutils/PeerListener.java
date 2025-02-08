package com.github.jaykkumar01.watchparty_duo.webviewutils;

public interface PeerListener {
    void onPeerOpen(String peerId);
    void onConnectionOpen(String peerId, String remoteId);

    void onBatchReceived(String jsonDataBytes);
}
