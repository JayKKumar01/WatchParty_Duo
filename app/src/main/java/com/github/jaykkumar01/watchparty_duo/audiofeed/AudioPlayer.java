package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayer {
    private AudioTrack audioTrack;
    private boolean isPlaying = false;

    public void start() {
        if (isPlaying) return;

        // Create audio track with optimized settings
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_OUT_CONFIG,
                AudioConfig.AUDIO_FORMAT,
                AudioConfig.BUFFER_OUT_SIZE,
                AudioTrack.MODE_STREAM
        );

        audioTrack.play();
        isPlaying = true;
        Log.d("AudioPlayer", "Audio playback started");
    }

    public void play(byte[] audioData) {
        if (!isPlaying || audioTrack == null) return;
        try {
            // Write data directly to hardware buffer
            audioTrack.write(audioData, 0, audioData.length);
        } catch (IllegalStateException e) {
            Log.e("AudioPlayer", "Error writing audio data: ", e);
            reset();
        }
    }

    public void stop() {
        if (!isPlaying) return;

        reset();
        Log.d("AudioPlayer", "Audio playback stopped");
    }

    private void reset() {
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.flush();
                audioTrack.release();
            } catch (IllegalStateException e) {
                Log.e("AudioPlayer", "Error resetting audio track: ", e);
            }
            audioTrack = null;
        }
        isPlaying = false;
    }
}