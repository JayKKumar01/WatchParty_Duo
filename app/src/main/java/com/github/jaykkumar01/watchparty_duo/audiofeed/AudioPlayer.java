package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.github.jaykkumar01.watchparty_duo.constants.Packets;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayer {
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private final AtomicBoolean isWriting = new AtomicBoolean(false);
    private final FeedListener feedListener;

    public AudioPlayer(FeedListener feedListener) {
        this.feedListener = feedListener;
    }

    public void start() {
        if (isPlaying) return;

        try {
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
            updateListener("Audio playback started");
            Log.d("AudioPlayer", "Audio playback started");
        } catch (Exception e) {
            Log.e("AudioPlayer", "Error initializing AudioTrack: ", e);
        }
    }

    public void play(byte[] audioData) {
        if (!isPlaying || audioTrack == null || isWriting.get()) return;

        synchronized (isWriting) {
            if (isWriting.get()) return; // Double-check within sync block
            isWriting.set(true);
        }

        try {
            audioTrack.write(audioData, 0, audioData.length);
            synchronized (this){
                Packets.audioPacketExecuted++;
            }
        } catch (IllegalStateException e) {
            Log.e("AudioPlayer", "Error writing audio data: ", e);
            reset();
        } finally {
            isWriting.set(false);
        }
    }


    public void stop() {
        if (!isPlaying) return;
        reset();
        updateListener("Audio playback stopped");
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

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
