package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class FeedModel implements Serializable {
    private ImageFeedModel imageFeedModel;
    private AudioFeedModel audioFeedModel;
    private SignalFeedModel signalFeedModel;

    private int feedType;
    private long timestamp;

    public FeedModel(ImageFeedModel imageFeedModel, AudioFeedModel audioFeedModel, SignalFeedModel signalFeedModel) {
        this.imageFeedModel = imageFeedModel;
        this.audioFeedModel = audioFeedModel;
        this.signalFeedModel = signalFeedModel;
    }

    public FeedModel(ImageFeedModel imageFeedModel) {
        this.imageFeedModel = imageFeedModel;
    }

    public FeedModel(int feedType) {
        this.feedType = feedType;
    }

    public ImageFeedModel getImageFeedModel() {
        return imageFeedModel;
    }

    public void setImageFeedModel(ImageFeedModel imageFeedModel) {
        this.imageFeedModel = imageFeedModel;
    }

    public AudioFeedModel getAudioFeedModel() {
        return audioFeedModel;
    }

    public void setAudioFeedModel(AudioFeedModel audioFeedModel) {
        this.audioFeedModel = audioFeedModel;
    }

    public SignalFeedModel getSignalFeedModel() {
        return signalFeedModel;
    }

    public void setSignalFeedModel(SignalFeedModel signalFeedModel) {
        this.signalFeedModel = signalFeedModel;
    }

    public int getFeedType() {
        return feedType;
    }

    public void setFeedType(int feedType) {
        this.feedType = feedType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
