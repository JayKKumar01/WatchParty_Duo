package com.github.jaykkumar01.watchparty_duo.models;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.github.jaykkumar01.watchparty_duo.interfaces.AudioData;


public class AudioPlayerModel implements AudioData{

    AudioTrack audioTrack;

    long offset;
    String id;

    public AudioPlayerModel(String id,long millis) {
        this.id = id;
        offset = System.currentTimeMillis() - millis;
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT,
                BUFFER_SIZE_IN_BYTES,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void processFile(byte[] bytes, int read, long millis, String id, float loudness) {

        long diff = System.currentTimeMillis() - millis - offset;
        if (diff > 500){
            return;
        }
        audioTrack.write(bytes, 0, read);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

