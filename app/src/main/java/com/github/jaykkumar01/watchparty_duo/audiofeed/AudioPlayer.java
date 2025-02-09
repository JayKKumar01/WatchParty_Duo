package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioPlayer {
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private boolean isWriting = false;
    private final FeedListener feedListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AudioPlayer(FeedListener feedListener){
        this.feedListener = feedListener;
        start();
    }

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
        if (!isPlaying || audioTrack == null || isWriting) return;
        executorService.execute(() -> {
            try {
                isWriting = true;
                // Write data directly to hardware buffer
                audioTrack.write(audioData, 0, audioData.length);
            } catch (IllegalStateException e) {
                Log.e("AudioPlayer", "Error writing audio data: ", e);
                reset();
            } finally {
                isWriting = false;
            }
        });
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

    private void updateListener(String logMessage) {
        if (feedListener != null){
            feedListener.onUpdate(logMessage);
        }
    }
}