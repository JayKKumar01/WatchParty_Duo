package com.github.jaykkumar01.watchparty_duo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.interfaces.AudioData;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioUtil  implements AudioData {
    private final Context context;
    private AudioRecord audioRecord;

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public AudioUtil(Context context) {
        this.context = context;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(
//                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE_IN_BYTES);
        }
    }

    public void startAudioTransfer() {
        audioRecord.startRecording();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler();
        executorService.execute(() -> {
            byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];
            while (true) {

                long millis = System.currentTimeMillis();
                if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
//                    Base.sleep(500);
                    continue;
                }
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE_IN_BYTES);
                float loudness = AudioCalculator.getLoudness(buffer);

//                if (!Base.isNetworkAvailable(context)) {
////                    Base.sleep(500);
//                    continue;
//                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ConnectionService.getInstance() != null){
                            ConnectionService.getInstance().sendAudioFile(buffer,read,millis,loudness);
                        }
                    }
                });
            }
        });
    }

    public void record(boolean isMute) {
        if (isMute){
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }

        }else {
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.startRecording();
            }
        }

    }




}
