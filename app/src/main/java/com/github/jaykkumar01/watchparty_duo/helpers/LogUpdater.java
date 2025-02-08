package com.github.jaykkumar01.watchparty_duo.helpers;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LogUpdater {

    private final TextView logTextView;
    private final ScrollView logScrollView;
    private final Handler handler = new Handler();
    private final List<String> logBuffer = new ArrayList<>();
    private boolean isUserScrolling = false;
    private final int updateInterval = 1000; // Update every 5 seconds

    public LogUpdater(TextView logTextView, ScrollView logScrollView) {
        this.logTextView = logTextView;
        this.logScrollView = logScrollView;
        startLogUpdater();
    }

    @SuppressLint("SetTextI18n")
    public void addLogMessage(String message) {
        synchronized (logBuffer) {
            logBuffer.add(message);
        }
    }

    private void startLogUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs();
                handler.postDelayed(this, updateInterval);
            }
        }, updateInterval);
    }

    @SuppressLint("SetTextI18n")
    private void updateLogs() {
        List<String> logsToAppend;
        synchronized (logBuffer) {
            if (logBuffer.isEmpty()) {
                return;
            }
            logsToAppend = new ArrayList<>(logBuffer);
            logBuffer.clear();
        }

        StringBuilder logBuilder = new StringBuilder();
        for (String log : logsToAppend) {
            logBuilder.append("\n").append(log);
        }

        logTextView.append(logBuilder.toString());

        // Auto-scroll only if user hasn't manually scrolled up
        if (!isUserScrolling) {
            logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    // Call this method to set the user's scrolling state
    public void setUserScrolling(boolean isScrolling) {
        isUserScrolling = isScrolling;
    }
}

