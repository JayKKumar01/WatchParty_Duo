package com.github.jaykkumar01.watchparty_duo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.interfaces.AudioData;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioUtil implements AudioData {
    private final Context context;
    private AudioRecord audioRecord;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean isRecording = false;

    private int fileSendCount = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable updateToastRunnable = new Runnable() {
        @Override
        public void run() {
            if (fileSendCount > 0) {
                Toast.makeText(context, "Files sent: " + fileSendCount + "/sec", Toast.LENGTH_SHORT).show();
            }
            fileSendCount = 0; // Reset count for the next second
            handler.postDelayed(this, 1000); // Repeat every 1 second
        }
    };

    public AudioUtil(Context context) {
        this.context = context;
    }

    public void stopRecording() {
        if (audioRecord == null || !isRecording) {
            return;
        }

        isRecording = false;
        audioRecord.stop();
        audioRecord.release(); // Properly release resources
        handler.removeCallbacks(updateToastRunnable); // Stop toast updates

        //executorService.shutdown(); // Stop background tasks
    }

    public void startRecording() {
        if (isRecording) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE_IN_BYTES);
        }
        audioRecord.startRecording();
        isRecording = true;

        // Start the toast updater
        handler.post(updateToastRunnable);


        executorService.execute(() -> {
            byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];
            // Get the connection service once before the loop
            ConnectionService connectionService = ConnectionService.getInstance();
            if (connectionService == null) {
                stopRecording(); // Stop recording if service is unavailable
                return;
            }

            while (isRecording && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                long millis = System.currentTimeMillis();
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE_IN_BYTES);
                float loudness = -1f;
//                float loudness = AudioCalculator.getLoudness(buffer);

                if (!Base.isNetworkAvailable(context)) {
                    continue;
                }

                connectionService.sendAudioFile(buffer, read, millis, loudness);
                fileSendCount++; // Increment count
            }
        });
    }



    public void record(boolean isMute) {
        if (isMute) {
            stopRecording();
        } else {
            startRecording();
        }
    }
}
