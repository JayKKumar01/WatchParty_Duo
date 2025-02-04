package com.github.jaykkumar01.watchparty_duo.transferfeeds;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.interfaces.AudioData;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.AudioCalculator;
import com.github.jaykkumar01.watchparty_duo.utils.Base;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioFeed implements AudioData {
    private final Context context;
    private AudioRecord audioRecord;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean isRecording = false;


    private final AudioManager audioManager;

    public AudioFeed(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void stopRecording() {
        if (audioRecord == null || !isRecording) {
            return;
        }

        isRecording = false;
        audioRecord.stop();
        audioRecord.release(); // Properly release resources

        //executorService.shutdown(); // Stop background tasks
    }

    public void startRecording() {
        if (isRecording || AppData.getInstance().isMute()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        // Check for Bluetooth headset and enable SCO
//        if (isBluetoothHeadsetConnected()) {
//            audioManager.startBluetoothSco(); // Enable Bluetooth SCO audio
//            audioManager.setBluetoothScoOn(true);
//            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
//        }

        audioRecord = new AudioRecord(
                audioSource,
                SAMPLE_RATE,
                CHANNEL_IN_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE_IN_BYTES
        );
        audioRecord.startRecording();
        isRecording = true;


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

                if (read <= 0 ){
                    continue;
                }
                float loudness = AudioCalculator.getLoudness(buffer);

                if (!Base.isNetworkAvailable(context)) {
                    continue;
                }

                connectionService.sendAudioFile(buffer, read, millis, loudness);
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

    private boolean isBluetoothHeadsetConnected() {
        // Check for Bluetooth permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ConnectionService.getInstance().showToast("No Bluetooth Permission");
            return false;
        }
        // Retrieve connected audio devices
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                return true;
            }
        }
        return false;
    }



}
