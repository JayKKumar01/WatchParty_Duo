package com.github.jaykkumar01.watchparty_duo.listeners;

public interface WebFeedListener {
    void onPeerOpen(String peerId);
    void onConnectionOpen(String peerId, String remoteId, int count);
    void onBatchReceived(String jsonData);

    void onUpdate(String message);

    void onConnectionClosed();

    void onMetaData(String jsonData);

    void onConnectionAlive(boolean isAlive);

    void onRestartPeer();

    void onRestartConnection();
}
