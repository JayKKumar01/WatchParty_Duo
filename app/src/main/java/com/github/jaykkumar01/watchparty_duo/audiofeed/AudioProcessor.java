package com.github.jaykkumar01.watchparty_duo.audiofeed;

import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;

public class AudioProcessor {
    private final FeedListener feedListener;

    public AudioProcessor(FeedListener feedListener) {
        this.feedListener = feedListener;
    }

    public void processAudio(byte[] audioData, long timestamp) {
        feedListener.onFeed(audioData,timestamp, FeedType.AUDIO_FEED);
    }
}
