package com.github.jaykkumar01.watchparty_duo.managers.helpers;

import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.helpers.ProcessFeed;
import com.github.jaykkumar01.watchparty_duo.helpers.WebSocketSender;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.models.PacketModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchPacketHandler {
    private final FeedManager feedManager;
    private final WebView webView;
    private final WebSocketSender webSocketSender;
    private final ProcessFeed processFeed;
    private final ExecutorService packetExecutor;
    private final Gson gson;
    private final PacketModel packetModel = new PacketModel();
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());

    public BatchPacketHandler(FeedManager feedManager,WebView webView) {
        this.feedManager = feedManager;
        this.webView = webView;
        this.webSocketSender = new WebSocketSender(feedManager);
        this.processFeed = new ProcessFeed(feedManager, feedManager);
        this.packetExecutor = Executors.newCachedThreadPool();
        this.gson = new Gson();
    }

    public void handleBatch(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) return;

        List<FeedModel> batch = gson.fromJson(jsonData, new TypeToken<List<FeedModel>>() {}.getType());
        if (batch == null || batch.isEmpty()) return;

        List<FeedModel> imageFeeds = new ArrayList<>();
        List<FeedModel> audioFeeds = new ArrayList<>();

        for (FeedModel model : batch) {
            if (model == null) continue;
            switch (model.getFeedType()) {
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

        if (!imageFeeds.isEmpty()) {
            packetExecutor.execute(() -> processFeed.processImageFeed(imageFeeds));
        }
        if (!audioFeeds.isEmpty()) {
            packetExecutor.execute(() -> processFeed.processAudioFeed(audioFeeds));
        }
    }

    public void destroy() {
        packetExecutor.shutdownNow();
        processFeed.stop();
        stopLoggingUpdates();
    }

    public void stopImageProcess() {
        processFeed.stopImageProcess();
    }

    public void startImageProcess() {
        processFeed.startImageProcess();
    }

    public void setTextureView(TextureView remoteFeed) {
        processFeed.setTextureView(remoteFeed);
    }

    public void deafenAudio(boolean isDeafen) {
        if (isDeafen){
            processFeed.stopAudioProcess();
        } else {
            processFeed.startAudioProcess();
        }
    }

    public void start() {
        webSocketSender.initializeSender();
        processFeed.start();
        processFeed.startAudioProcess();
        startLoggingUpdates();
    }

    public void onFeed(byte[] bytes, long millis, int feedType) {
        webSocketSender.addData(bytes,millis,feedType);
        if (feedType == FeedType.IMAGE_FEED) {
            packetModel.imageFeedSent();
        }else if (feedType == FeedType.AUDIO_FEED) {
            packetModel.audioFeedSent();
        }
    }

    private final Runnable logUpdater = new Runnable() {
        @Override
        public void run() {
            feedManager.onUpdate(packetModel.toString());
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
}
