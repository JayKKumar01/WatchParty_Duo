package com.github.jaykkumar01.watchparty_duo.models;

public class ImageFeedModel extends BaseFeedModel {
    public ImageFeedModel(byte[] rawData, String base64Data) {
        super(rawData, base64Data);
    }

    public ImageFeedModel(byte[] rawData) {
        super(rawData);
    }

    public ImageFeedModel(String base64Data) {
        super(base64Data);
    }
}

