package com.github.jaykkumar01.watchparty_duo.listeners;

public interface ForegroundNotifier {
    void updateNotification(boolean isConnectionOpen);
    void onUpdateLogs(String logMessage);

    void onConnectionClosed();
}

