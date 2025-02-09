package com.github.jaykkumar01.watchparty_duo.models;

import androidx.annotation.NonNull;

public class PacketModel{
    private int imageFeedSent;
    private int imageFeedReceived;
    private int audioFeedSent;
    private int audioFeedReceived;

    public PacketModel() {
        this.imageFeedSent = 0;
        this.imageFeedReceived = 0;
        this.audioFeedSent = 0;
        this.audioFeedReceived = 0;
    }

    public void reset() {
        imageFeedSent = 0;
        imageFeedReceived = 0;
        audioFeedSent = 0;
        audioFeedReceived = 0;
    }

    public void imageFeedSent() {
        imageFeedSent++;
    }

    public void imageFeedReceived() {
        imageFeedReceived++;
    }

    public void audioFeedSent() {
        audioFeedSent++;
    }

    public void audioFeedReceived() {
        audioFeedReceived++;
    }

    public int getImageFeedSent() {
        return imageFeedSent;
    }

    public int getImageFeedReceived() {
        return imageFeedReceived;
    }

    public int getAudioFeedSent() {
        return audioFeedSent;
    }

    public int getAudioFeedReceived() {
        return audioFeedReceived;
    }

    @NonNull
    @Override
    public String toString() {
        return "Image -> [" + imageFeedSent + " : " + imageFeedReceived + "], " +
                "Audio ->[" + audioFeedSent + " : " + audioFeedReceived+"]";
    }

}
