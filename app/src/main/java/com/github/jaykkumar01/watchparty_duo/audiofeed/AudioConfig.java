// AudioConfig.java
package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.media.AudioRecord;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;

public class AudioConfig {
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = Math.max(
            (SAMPLE_RATE * 2) / Feed.UPS, // 16-bit = 2 bytes, mono = 1 channel
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    );
}