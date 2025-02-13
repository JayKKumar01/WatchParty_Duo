package com.github.jaykkumar01.watchparty_duo.audiofeed;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.utils.Base;

public class AudioRecorder {
    private AudioRecord audioRecord;
    private volatile boolean isRecording = false;
    private Thread recordingThread;
    private final Context context;

    public AudioRecorder(Context context) {
        this.context = context.getApplicationContext();
    }

    public void startRecording(AudioProcessor processor) {
        if (isRecording || ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        audioRecord = new AudioRecord(
//                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_IN_CONFIG,
                AudioConfig.AUDIO_FORMAT,
                AudioConfig.BUFFER_IN_SIZE
        );

        isRecording = true;
        recordingThread = new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            byte[] buffer = new byte[AudioConfig.BUFFER_IN_SIZE];
            try {
                audioRecord.startRecording();
                while (isRecording) {
                    long timestamp = System.currentTimeMillis();
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read <= 0) {
                        break; // Handle read errors
                    }
                    processor.processAudio(buffer, timestamp);
                }
            } finally {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                isRecording = false;
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void stopRecording() {
        isRecording = false;
        if (recordingThread != null) {
            try {
                recordingThread.join(); // Wait for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            recordingThread = null;
        }
    }
}