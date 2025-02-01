package com.github.jaykkumar01.watchparty_duo.interfaces;

import android.media.AudioFormat;

public interface AudioData {
    int SAMPLE_RATE = 22000;
    int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int BYTES_PER_SAMPLE = 2; // 16-bit audio (2 bytes per sample)
    int BUFFER_SIZE_IN_BYTES = 8 * 1024;
    float MAX_AMP = 32768;
}
