package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;

public class AudioRecorder {
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private final Handler backgroundHandler;

    public AudioRecorder(Context context) {
        HandlerThread handlerThread = new HandlerThread("AudioRecordingThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_CONFIG,
                AudioConfig.AUDIO_FORMAT,
                AudioConfig.BUFFER_SIZE);
    }

    public void startRecording(AudioProcessor processor) {
        isRecording = true;
        backgroundHandler.post(() -> {
            byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
            audioRecord.startRecording();
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    processor.processAudio(buffer, System.currentTimeMillis());
                }
            }
        });
    }

    public void stopRecording() {
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
    }
}
