package com.github.jaykkumar01.watchparty_duo.interfaces;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.MainActivity;
import com.github.jaykkumar01.watchparty_duo.activities.PlayerActivity;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;

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
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("onPeerOpen: "+peerId);
        }
        if (AppData.getInstance().isConnectionEstablished()){
            return;
        }
        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().onPeerOpen(peerId);
        }
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().onPeerOpen(peerId);
        }

    }
    @JavascriptInterface
    public void onConnectionOpen(String remoteId){
        AppData.getInstance().setConnectionActive(true);
        if (MainActivity.getInstance() != null && !AppData.getInstance().isConnectionEstablished()){
            AppData.getInstance().setConnectionEstablished(true);
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
    public void onConnectionAlive(boolean isAlive){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("isConnectionAlive: "+isAlive);
        }
        if (!isAlive){
            AppData.getInstance().setConnectionActive(false);
            if (ConnectionService.getInstance() != null){
                ConnectionService.getInstance().updateNotification();
                ConnectionService.getInstance().stopAudioTransfer();
            }
        }

//        Toast.makeText(context, "isConnectionAlive: "+isAlive, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onNextPeer(String nextPeerId, String nextRemoteId){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("nextPeerId: "+nextPeerId+", nextRemoteId: "+nextRemoteId);
        }
//        Toast.makeText(context, "nextPeerId: "+nextPeerId+", nextRemoteId: "+nextRemoteId, Toast.LENGTH_SHORT).show();
    }


    @JavascriptInterface
    public void onPeerDisconnected(String peerId){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("onPeerDisconnected: "+peerId);
        }

//        Toast.makeText(context, "onPeerDisconnected: "+peerId, Toast.LENGTH_SHORT).show();

    }
    @JavascriptInterface
    public void onPeerClose(String peerId){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("onPeerClose: "+ peerId);
        }
//        Toast.makeText(context, "onPeerClose: "+ peerId, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onConnectionClose(String remoteId){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("onConnectionClose: "+ remoteId);
        }
//        Toast.makeText(context, "onConnectionClose: "+ remoteId, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onConnectionError(String err){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("Error: "+ err);
        }
//        Toast.makeText(context, "Error: "+ err, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onPeerReopened(String peerId){
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("onPeerReopen: "+peerId);
        }
//        Toast.makeText(context, "onPeerReopen: "+peerId, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onRetryConnection(int attempt) {
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("Connection Attempt #" + attempt);
        }
//        Toast.makeText(context, "Connection Attempt #" + attempt, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void onRetryPeer(int attempt) {
        if (PlayerActivity.getInstance() != null){
            PlayerActivity.getInstance().updateLog("Peer Attempt #" + attempt);
        }
//        Toast.makeText(context, "Peer Attempt #" + attempt, Toast.LENGTH_SHORT).show();
    }



    @JavascriptInterface
    public void sendToast(String toast){
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }



}
