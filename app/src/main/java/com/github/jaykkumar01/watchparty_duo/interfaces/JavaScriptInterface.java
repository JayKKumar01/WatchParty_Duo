package com.github.jaykkumar01.watchparty_duo.interfaces;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.MainActivity;
import com.github.jaykkumar01.watchparty_duo.models.AudioPlayerModel;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptInterface {
    private Context context;

    public JavaScriptInterface(Context context){
        this.context = context;
    }

    private final ConcurrentHashMap<String, AudioPlayerModel> playerMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @JavascriptInterface
    public void onPeer(String peerId){
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().onPeer();
        }
        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().onPeer(peerId);
        }

    }
    @JavascriptInterface
    public void onConnected(String remoteId){
        if (MainActivity.getInstance() != null){
            MainActivity.getInstance().onConnected(remoteId);
        }
        if (ConnectionService.getInstance() != null){
            ConnectionService.getInstance().updateNotification();
            ConnectionService.getInstance().startAudioTransfer();
        }
    }

    @JavascriptInterface
    public void readAudioFile(String id, byte[] bytes, int read, long millis, float loudness){

        playerMap.putIfAbsent(id, new AudioPlayerModel(id, millis));

        AudioPlayerModel audioPlayerModel = playerMap.get(id);

        executorService.execute(() -> audioPlayerModel.processFile(bytes,read,millis,id,loudness));

    }

    @JavascriptInterface
    public void onDisconnected(String remoteId){
        Toast.makeText(context, "Disconnected: "+remoteId, Toast.LENGTH_SHORT).show();

    }
    @JavascriptInterface
    public void onClose(String remoteId){
        Toast.makeText(context, "Closed: "+ remoteId, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void sendToast(String toast){
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }



}
