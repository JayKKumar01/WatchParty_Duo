package com.github.jaykkumar01.watchparty_duo.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.activities.FeedActivity;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioFeed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.helpers.ProcessFeed;
import com.github.jaykkumar01.watchparty_duo.imagefeed.ImageFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.ForegroundNotifier;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.models.PacketModel;
import com.github.jaykkumar01.watchparty_duo.helpers.WebSocketSender;
import com.github.jaykkumar01.watchparty_duo.webfeed.WebFeed;
import com.github.jaykkumar01.watchparty_duo.webfeed.WebFeedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedManager implements FeedListener,WebFeedListener{
    private final ForegroundNotifier foregroundNotifier;
    private final WebFeed webFeed;
    private final AudioFeed audioFeed;
    private final ImageFeed imageFeed;

    private final Context context;
    private final WebSocketSender webSocketSender;
    private final ProcessFeed processFeed;
    private ExecutorService packetExecutor = Executors.newCachedThreadPool();
    private final Gson gson = new Gson();
    private final PacketModel packetModel = new PacketModel();
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());


    public FeedManager(Context context, ForegroundNotifier foregroundNotifier) {
        this.context = context;
        this.foregroundNotifier = foregroundNotifier;
        this.webFeed = new WebFeed(context,this);
        this.audioFeed = new AudioFeed(context,this);
        this.imageFeed = new ImageFeed(context,this);
        this.webSocketSender = new WebSocketSender(context);
        this.webSocketSender.setForegroundNotifier(foregroundNotifier);
        this.processFeed = new ProcessFeed(this);
    }

    public void restartImageFeed(boolean isRestarting) {
        if (isRestarting){
            startImageFeed();
        }else {
            stopImageFeed();
        }
    }


    private void startLoggingUpdates() {
        updateLogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                foregroundNotifier.onUpdateLogs(packetModel.toString());
                packetModel.reset();
                // Continue updating every second
                updateLogHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public void startWebFeed(){
        webFeed.start();
    }

    public void connect(String remoteId) {
        webFeed.connect(remoteId);
    }

    public void startAudioFeed(){
        audioFeed.start();
    }

    public void startImageFeed(){
        imageFeed.initializeCamera();
    }

    public void stopImageFeed(){
        imageFeed.releaseResources();
    }


    public void setFeedSurfaces(TextureView peerFeed, TextureView remoteFeed) {
        imageFeed.setTextureView(peerFeed);
        processFeed.setTextureView(remoteFeed);
    }

    public void startFeeds() {
        webFeed.start();
        audioFeed.start();
        imageFeed.initializeCamera();
    }

    public void stopFeeds() {
        imageFeed.releaseResources();
        webFeed.stop();
        audioFeed.stop();
    }

    public void muteAudio(boolean mute) {
        if (mute) {
            audioFeed.stop();
        } else {
            audioFeed.start();
        }
    }
    public void deafenAudio(boolean isDeafen) {
        if (isDeafen){
            processFeed.stopAudioProcess();
        } else {
            processFeed.startAudioProcess();
        }
    }

    @Override
    public void onFeed(byte[] bytes, long millis, int feedType) {
        webSocketSender.addData(bytes,millis,feedType);
        if (feedType == FeedType.IMAGE_FEED) {
            packetModel.imageFeedSent();
        }else if (feedType == FeedType.AUDIO_FEED) {
            packetModel.audioFeedSent();
        }
    }

    @Override
    public void onError(String err) {

    }

    @Override
    public void onUpdate(String logMessage) {
        foregroundNotifier.onUpdateLogs(logMessage);
    }

    @Override
    public void onConnectionClosed() {
        foregroundNotifier.onConnectionClosed();
    }

    @Override
    public void onPeerOpen(String peerId) {
        FeedActivity feedActivity = FeedActivity.getInstance();
        if (feedActivity != null){
            feedActivity.onPeerOpen(peerId);
        }
        webFeed.onPeerOpen(peerId);
    }

    @Override
    public void onConnectionOpen(String peerId, String remoteId) {
        startLoggingUpdates();
        foregroundNotifier.updateNotification(true);
        FeedActivity feedActivity = FeedActivity.getInstance();
        if (feedActivity != null){
            feedActivity.onConnectionOpen(peerId,remoteId);
        }
        webFeed.onConnectionOpen(peerId,remoteId);
        webSocketSender.initializeSender(webFeed.getWebView());
        processFeed.startAudioProcess();
        startImageFeed();
    }

    @Override
    public void onBatchReceived(String jsonData) {
        if (packetExecutor.isShutdown()){
            packetExecutor = Executors.newCachedThreadPool();
        }
        packetExecutor.execute(() -> {
            try {

                List<FeedModel> batch = gson.fromJson(
                        jsonData,
                        new TypeToken<List<FeedModel>>(){}.getType()
                );

                List<FeedModel> imageFeeds = new ArrayList<>();
                List<FeedModel> audioFeeds = new ArrayList<>();

                for (FeedModel model: batch){
                    switch (model.getFeedType()){
                        case FeedType.IMAGE_FEED:
                            packetModel.imageFeedReceived();
                            imageFeeds.add(model);
                            break;
                        case FeedType.AUDIO_FEED:
                            packetModel.audioFeedReceived();
                            audioFeeds.add(model);
                            break;
                    }
                }
                Executors.newCachedThreadPool().execute(() -> processFeed.processImageFeed(imageFeeds));
                Executors.newCachedThreadPool().execute(() -> processFeed.processAudioFeed(audioFeeds));

            } catch (Exception e) {
                Log.e("WebSocketReceiver", "Error processing batch", e);
            }
        });

    }
}
