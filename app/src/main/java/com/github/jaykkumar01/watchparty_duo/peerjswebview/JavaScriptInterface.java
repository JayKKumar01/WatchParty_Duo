package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.github.jaykkumar01.watchparty_duo.listeners.PeerListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptInterface {
    private final PeerListener peerListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public JavaScriptInterface(Context context, PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @JavascriptInterface
    public void onPeerOpen(String peerId){
        peerListener.onPeerOpen(peerId);
    }
    @JavascriptInterface
    public void onConnectionOpen(String peerId,String remoteId){
        peerListener.onConnectionOpen(peerId,remoteId);
    }

    @JavascriptInterface
    public void onBatchReceived(String jsonData) {
        peerListener.onBatchReceived(jsonData);
    }

}
