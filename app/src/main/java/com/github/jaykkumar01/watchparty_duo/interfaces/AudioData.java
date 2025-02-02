package com.github.jaykkumar01.watchparty_duo.interfaces;

import android.media.AudioFormat;

public interface AudioData {
    boolean isMono = true; // Stereo enabled

    int SAMPLE_RATE = 16000; // Lower sample rate for reduced data size while keeping decent quality
    int CHANNEL_IN_CONFIG = isMono ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO; // Input config
    int CHANNEL_OUT_CONFIG = isMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO; // Output config
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16-bit PCM for balanced quality and size
    int BYTES_PER_SAMPLE = 2; // 16-bit audio (2 bytes per sample)

    // Further reduced buffer size for minimal latency and fast transmission
    int BUFFER_SIZE_IN_BYTES = 8 * 1024; // Smaller buffer to decrease transmission delay

    float MAX_AMP = 32767; // Correct max amplitude for 16-bit PCM
}





