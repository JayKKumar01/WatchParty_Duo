package com.github.jaykkumar01.watchparty_duo.updates;

public class AppData {
    private static final AppData instance = new AppData();

    private boolean connectionEstablished;
    private boolean connectionActive;
    private boolean mute;
    private int FPS = 3;
    private int imageHeight = 1;



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

    public int getFPS() {
        return Math.max(1,FPS);
    }

    public void setFPS(int FPS) {
        this.FPS = FPS;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
}

