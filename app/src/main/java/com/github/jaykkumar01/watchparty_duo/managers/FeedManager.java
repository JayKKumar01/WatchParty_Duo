package com.github.jaykkumar01.watchparty_duo.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.activities.FeedActivity;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioFeed;
import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.constants.Metadata;
import com.github.jaykkumar01.watchparty_duo.constants.Packets;
import com.github.jaykkumar01.watchparty_duo.helpers.ProcessFeed;
import com.github.jaykkumar01.watchparty_duo.imagefeed.ImageFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.ForegroundNotifier;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.models.PacketModel;
import com.github.jaykkumar01.watchparty_duo.helpers.WebSocketSender;
import com.github.jaykkumar01.watchparty_duo.webfeed.WebFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.WebFeedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedManager implements FeedListener,WebFeedListener{
    private final ForegroundNotifier foregroundNotifier;
    private final WebFeed webFeed;
    private AudioFeed audioFeed;
    private ImageFeed imageFeed;

    private final Context context;
    private WebSocketSender webSocketSender;
    private final ProcessFeed processFeed;
    private ExecutorService packetExecutor = Executors.newCachedThreadPool();
    private final Gson gson = new Gson();
    private final PacketModel packetModel = new PacketModel();
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());
    private long offset = -1;


    public FeedManager(Context context, ForegroundNotifier foregroundNotifier) {
        this.context = context;
        this.foregroundNotifier = foregroundNotifier;
        this.webFeed = new WebFeed(context,this);


        this.processFeed = new ProcessFeed(this);
    }

    public void onActivityStateChanged(boolean isRestarting, boolean isVideo) {
        if (isRestarting && isVideo){
            startImageFeed();
            startImageProcessFeed();
        }else {
            stopImageFeed();
            stopImageProcessFeed();
        }
    }

    private void stopImageProcessFeed() {
        processFeed.stopImageProcess();
    }

    private void startImageProcessFeed() {
        processFeed.startImageProcess();
    }

    private final Runnable logUpdater = new Runnable() {
        @Override
        public void run() {
            foregroundNotifier.onUpdateLogs(packetModel.toString());
            packetModel.reset();
            updateLogHandler.postDelayed(this, 1000); // Continue updating every second
        }
    };

    private void startLoggingUpdates() {
        updateLogHandler.post(logUpdater); // Start the loop
    }
    private void stopLoggingUpdates() {
        updateLogHandler.removeCallbacks(logUpdater); // Stop only this specific task
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

    public void isVideo(boolean isVideo) {
        if (isVideo){
            imageFeed.initializeCamera();
        }else {
            imageFeed.releaseResources();
        }
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
    public void onMetaData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        // Convert JSON string to a Map
        Map<String, Integer> map = gson.fromJson(
                jsonData,
                new TypeToken<Map<String, Integer>>() {}.getType()
        );

        // Extract values safely
        Integer latency = map.get(Metadata.LATENCY);
        Integer resolution = map.get(Metadata.RESOLUTION);
        Integer fps = map.get(Metadata.FPS);

        // Assign values to Feed class
        if (latency != null) {
            Feed.LATENCY = latency;
        }
        if (resolution != null) {
            Feed.RESOLUTION = resolution;
        }
        if (fps != null) {
            Feed.FPS = fps;
        }
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
    public void onConnectionOpen(String peerId, String remoteId, int count) {
        if (count > 0){
            foregroundNotifier.onUpdateLogs("Already Connected: "+count);
            return;
        }


        this.audioFeed = new AudioFeed(context,this);
        this.imageFeed = new ImageFeed(context,this);
        this.webSocketSender = new WebSocketSender(context,foregroundNotifier);
        this.processFeed.start();

        updateConnectionStatus(peerId,remoteId);
    }

    private void updateConnectionStatus(String peerId, String remoteId){
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
        if (packetExecutor.isShutdown()) {
            packetExecutor = Executors.newCachedThreadPool();
        }

        packetExecutor.execute(() -> {
            try {
                List<FeedModel> batch = gson.fromJson(
                        jsonData,
                        new TypeToken<List<FeedModel>>(){}.getType()
                );

                if (batch.isEmpty()){
                    return;
                }

                long firstTimestamp = batch.get(0).getTimestamp();

                synchronized (this) {
                    if (offset == -1) {
                        offset = System.currentTimeMillis() - firstTimestamp;
                    }
                }

                long adjustedTime = firstTimestamp + offset;
                long delay = System.currentTimeMillis() - adjustedTime;

                // Skip processing if delay exceeds 1000ms
                if (delay > Feed.LATENCY+100) {
                    return;
                }

                List<FeedModel> imageFeeds = new ArrayList<>();
                List<FeedModel> audioFeeds = new ArrayList<>();

                for (FeedModel model : batch) {

                    switch (model.getFeedType()) {
                        case FeedType.IMAGE_FEED:
                            packetModel.imageFeedReceived();
                            imageFeeds.add(model);
                            synchronized (this) {
                                Packets.imagePacketReceived++;
                            }
                            break;
                        case FeedType.AUDIO_FEED:
                            packetModel.audioFeedReceived();
                            audioFeeds.add(model);
                            synchronized (this) {
                                Packets.audioPacketReceived++;
                            }
                            break;
                    }
                }

                // Process only if there are valid feeds to handle
                if (!imageFeeds.isEmpty()) {
                    if (packetExecutor.isShutdown()){
                        packetExecutor = Executors.newCachedThreadPool();
                    }
                    packetExecutor.execute(() -> processFeed.processImageFeed(imageFeeds));
                }

                if (!audioFeeds.isEmpty()) {
                    if (packetExecutor.isShutdown()){
                        packetExecutor = Executors.newCachedThreadPool();
                    }
                    packetExecutor.execute(() -> processFeed.processAudioFeed(audioFeeds));
                }

            } catch (Exception e) {
                Log.e("WebSocketReceiver", "Error processing batch", e);
            }
        });
    }



    public void destroy() {
        stopFeeds();
        stopLoggingUpdates();
        processFeed.stop();
    }
}
