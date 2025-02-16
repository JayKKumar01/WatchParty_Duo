package com.github.jaykkumar01.watchparty_duo.listeners;

public interface ForegroundNotifier {
    void updateNotification(boolean isConnectionOpen);
    void onUpdateLogs(String logMessage);

    void onConnectionClosed();

    void onConnectionStatus(boolean isConnectionAlive);

    void onPeerRestart();

    void onRestartConnection();

    void onPeerRetryLimitReached();

    void onPlaybackUpdate(String jsonData);

    void onPeerError();
}

