package com.github.jaykkumar01.watchparty_duo.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioPlayer;
import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.Packets;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessFeed {
    private final FeedManager feedManager;
    private TextureView textureView;
    private final FeedListener feedListener;
    private final AudioPlayer audioPlayer;
    private ScheduledExecutorService imageScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService imageProcessingExecutor = Executors.newCachedThreadPool();

    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);
    private final TextureRenderer textureRenderer;
    private final Handler logHandler = new Handler(Looper.getMainLooper());


    public ProcessFeed(FeedListener feedListener, FeedManager feedManager) {
        this.feedListener = feedListener;
        this.feedManager = feedManager;
        this.audioPlayer = new AudioPlayer(feedListener);
        this.textureRenderer = new TextureRenderer(feedListener, false);
    }

    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
    }

    public void startAudioProcess() {
        audioPlayer.start();
    }

    public void stopAudioProcess() {
        audioPlayer.stop();
    }

    public void startImageProcess() {
        stopImageProcessing.set(false);
        updateListener("Image processing started");
    }

    public void stopImageProcess() {
        stopImageProcessing.set(true);
        if (imageScheduler != null && !imageScheduler.isShutdown()){
            imageScheduler.shutdownNow();
        }
        if (imageProcessingExecutor != null && !imageScheduler.isShutdown()){
            imageProcessingExecutor.shutdownNow();
        }
        updateListener("Image processing stopped");
    }

    public void processAudioFeed(List<FeedModel> models) {
        for (FeedModel model : models) {
            String base64 = model.getAudioFeedModel().getBase64Data();
            byte[] audioBytes = Base64.decode(base64,Base64.NO_WRAP);
            if (audioBytes == null || audioBytes.length == 0) continue;
            audioPlayer.play(audioBytes);
        }
    }

    private final List<Long> firstPacketTimes = new ArrayList<>();
    private final List<Long> firstArrivals = new ArrayList<>();
    private static final int INITIAL_PACKETS = 4; // Consider first 4 packets for average
    private int packetNumber = 0;

    public void processImageFeed(List<FeedModel> models) {
        if (models.isEmpty() || stopImageProcessing.get()) return;

        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            if (firstPacketTimes.size() < INITIAL_PACKETS) {
                firstPacketTimes.add(models.get(0).getTimestamp());
                firstArrivals.add(currentTime);
            }
            packetNumber++;
        }

        long avgPacketTime = firstPacketTimes.stream().mapToLong(Long::longValue).sum() / firstPacketTimes.size();
        long avgArrivalTime = firstArrivals.stream().mapToLong(Long::longValue).sum() / firstArrivals.size();

        long packetDelay = currentTime - avgArrivalTime;
        feedManager.onUpdate("\n" + packetNumber + ". Packet Delay: " + packetDelay);

        for (FeedModel model : models) {
            long expectedDelay = model.getTimestamp() - avgPacketTime;
            long scheduleAfter = expectedDelay - packetDelay;

            feedManager.onUpdate("Expected Delay: " + expectedDelay + ", Scheduled After: " + scheduleAfter);

            if (scheduleAfter < 0) continue;

            synchronized (this) {
                if (imageScheduler.isShutdown()) {
                    imageScheduler = Executors.newSingleThreadScheduledExecutor();
                }
            }
            imageScheduler.schedule(() -> renderImage(model), scheduleAfter, TimeUnit.MILLISECONDS);
        }
    }




    private void renderImage(FeedModel model) {
        if (stopImageProcessing.get()) {
            return;
        }

        synchronized (isProcessingImage) {
            if (isProcessingImage.get()) {
                return;
            }
            isProcessingImage.set(true);
        }

        synchronized (this) {
            if (imageProcessingExecutor.isShutdown()) {
                imageProcessingExecutor = Executors.newCachedThreadPool();
            }
        }
        imageProcessingExecutor.execute(() -> {
            try {
                String base64 = model.getImageFeedModel().getBase64Data();
                byte[] imageBytes = Base64.decode(base64, Base64.NO_WRAP);
                if (imageBytes == null || imageBytes.length == 0) {
                    return;
                }
                textureRenderer.updateTexture(textureView, imageBytes);

            } catch (Exception e) {
                Log.e("ProcessFeed", "Error processing image feed", e);
                feedManager.onUpdate("Error: "+e);
            } finally {
                isProcessingImage.set(false);
            }

        });
    }

    private final Runnable logRunnable = new Runnable() {
        @Override
        public void run() {
            updateListener("Image Packet Wasted: "+ (Packets.imagePacketReceived - Packets.imagePacketExecuted));
            updateListener("Audio Packet Wasted: "+ (Packets.audioPacketReceived - Packets.audioPacketExecuted));
            logHandler.postDelayed(this, 1000);
        }
    };
    public void start() {
        logHandler.post(logRunnable);
    }
    public void stop(){
        logHandler.removeCallbacks(logRunnable);
        stopImageProcess();
    }

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
