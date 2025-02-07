package com.github.jaykkumar01.watchparty_duo.updates;

public class AppData {
    private static final AppData instance = new AppData(); // packet loss, texture load issue
    public static final int FPS = 11;
    public static final int IMAGE_HEIGHT = 144;
    public static final int LATENCY_DELAY = 333; // in ms

    private boolean connectionEstablished;
    private boolean connectionActive;
    private boolean mute;

    private AppData() {} // Private constructor to prevent instantiation

    public static AppData getInstance() {
        return instance;
    }

    public boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    public void setConnectionEstablished(boolean connectionEstablished) {
        this.connectionEstablished = connectionEstablished;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public void reset(){
        connectionEstablished = false;
        mute = false;
    }

    public boolean isConnectionActive() {
        return connectionActive;
    }

    public void setConnectionActive(boolean connectionActive) {
        this.connectionActive = connectionActive;
    }


}

