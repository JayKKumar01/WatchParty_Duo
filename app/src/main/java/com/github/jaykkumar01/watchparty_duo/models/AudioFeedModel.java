package com.github.jaykkumar01.watchparty_duo.models;

import java.io.Serializable;

public class AudioFeedModel extends BaseFeedModel {
    public AudioFeedModel(byte[] rawData, String base64Data) {
        super(rawData, base64Data);
    }

    public AudioFeedModel(String base64Data) {
        super(base64Data);
    }
}

