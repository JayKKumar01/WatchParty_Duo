package com.github.jaykkumar01.watchparty_duo.audiofeed;

public class AudioConfig {
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = android.media.AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
}
