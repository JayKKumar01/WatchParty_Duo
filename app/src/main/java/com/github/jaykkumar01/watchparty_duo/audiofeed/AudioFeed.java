package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.content.Context;

import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioRecorder;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;

public class AudioFeed {
    private final AudioRecorder audioRecorder;
    private final AudioProcessor audioProcessor;

    public AudioFeed(Context context, FeedListener feedListener) {
        this.audioRecorder = new AudioRecorder(context);
        this.audioProcessor = new AudioProcessor(feedListener);
    }

    public void start() {
        audioRecorder.startRecording(audioProcessor);
    }

    public void stop() {
        audioRecorder.stopRecording();
    }
}
