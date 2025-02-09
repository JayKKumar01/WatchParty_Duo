// AudioConfig.java
package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;

public class AudioConfig {
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_IN_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);
    public static final int BUFFER_OUT_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_CONFIG, AUDIO_FORMAT);
}