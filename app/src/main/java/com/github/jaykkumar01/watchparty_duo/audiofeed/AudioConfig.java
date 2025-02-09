package com.github.jaykkumar01.watchparty_duo.audiofeed;

import static com.github.jaykkumar01.watchparty_duo.constants.Feed.AUDIO_BUFFER_PER_SECOND;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;

public class AudioConfig {
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Theoretical buffer size needed for 1 second of audio (16-bit PCM, mono)
    public static final int BUFFER_ONE_SECOND = SAMPLE_RATE * 2; // 16-bit = 2 bytes per sample

    // Set buffer sizes to the max of minBufferSize and 1 second of audio
    public static final int BUFFER_IN_SIZE = Math.max(
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT),
            BUFFER_ONE_SECOND / AUDIO_BUFFER_PER_SECOND
    );

    public static final int BUFFER_OUT_SIZE = Math.max(
            AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_CONFIG, AUDIO_FORMAT),
            BUFFER_ONE_SECOND / AUDIO_BUFFER_PER_SECOND
    );
}
