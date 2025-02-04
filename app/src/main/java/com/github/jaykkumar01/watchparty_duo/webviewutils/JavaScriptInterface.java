package com.github.jaykkumar01.watchparty_duo.webviewutils;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterface {
    private final Peer peer;
    private final PeerListener peerListener;
    private final Handler mainHandler;
    private final Context context;

    public JavaScriptInterface(Context context, Peer peer, PeerListener peerListener, Handler mainHandler) {
        this.context = context;
        this.peer = peer;
        this.peerListener = peerListener;
        this.mainHandler = mainHandler;
    }

    // Helper method to ensure callbacks are executed on the main thread
    private void runOnMainThread(Runnable task) {
        if (mainHandler != null) {
            mainHandler.post(task);
        } else {
            task.run(); // Fallback in case mainHandler is null
        }
    }

    @JavascriptInterface
    public void onPeerOpen(String peerId){
        peer.setPeerOpen(true);
        runOnMainThread(() -> peerListener.onPeerOpen(peerId));
    }
    @JavascriptInterface
    public void onConnectionOpen(String peerId,String remoteId){
        peer.setConnectionOpen(true);
        runOnMainThread(() -> peerListener.onConnectionOpen(peerId,remoteId));
    }
    @JavascriptInterface
    public void readImageFeed(String peerId, byte[] imageFeedBytes, long millis){
        runOnMainThread(() -> peerListener.onReadImageFeed(peerId,imageFeedBytes,millis));
    }

}
