package com.github.jaykkumar01.watchparty_duo.interfaces;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.MainActivity;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptInterface implements AudioData{
    private final Context context;
    private AudioTrack audioTrack;
    private long offset;

    public JavaScriptInterface(Context context){
        this.context = context;
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @JavascriptInterface
    public void onPeerOpen(String peerId){
        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().onPeerOpen(peerId);
        }
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().onPeerOpen(peerId);
        }

    }
    @JavascriptInterface
    public void onConnectionOpen(String remoteId){
        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().onConnectionOpen(remoteId);
        }
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().updateNotification();
            ConnectionService.getInstance().startAudioTransfer();
        }
    }

    @JavascriptInterface
    public void readAudioFile(String id, byte[] bytes, int read, long millis, float loudness){
        if (audioTrack == null){
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    CHANNEL_OUT_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE_IN_BYTES,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
            offset = System.currentTimeMillis() - millis;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                long diff = System.currentTimeMillis() - millis - offset;
                if (diff < 0){
                    audioTrack.write(bytes, 0, read);
                }

            }
        });
    }

    @JavascriptInterface
    public void onPeerDisconnected(String peerId){

        Toast.makeText(context, "onPeerDisconnected: "+peerId, Toast.LENGTH_SHORT).show();

    }
    @JavascriptInterface
    public void onPeerClose(String peerId){
        Toast.makeText(context, "onPeerClose: "+ peerId, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onConnectionClose(String remoteId){
        Toast.makeText(context, "onConnectionClose: "+ remoteId, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void sendToast(String toast){
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }



}
