package com.github.jaykkumar01.watchparty_duo.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioPlayer;
import com.github.jaykkumar01.watchparty_duo.constants.Packets;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessFeed {
    private TextureView textureView;
    private final FeedListener feedListener;
    private final AudioPlayer audioPlayer;
    private ScheduledExecutorService imageScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService imageProcessingExecutor = Executors.newCachedThreadPool();

    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);
    private final TextureRenderer textureRenderer;

    private int framesDrawn = 0;
    private int framesSkipped = 0;
    private int framesReturned = 0;
    private final Handler logHandler = new Handler(Looper.getMainLooper());

    public ProcessFeed(FeedListener feedListener) {
        this.feedListener = feedListener;
        this.audioPlayer = new AudioPlayer(feedListener);
        this.textureRenderer = new TextureRenderer(feedListener, false);
        startLogging();
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
        imageScheduler.shutdownNow();
        imageProcessingExecutor.shutdownNow();
        updateListener("Image processing stopped");
    }

    public void processAudioFeed(List<FeedModel> models) {
        for (FeedModel model : models) {
            byte[] audioBytes = model.getBase64Bytes();
            if (audioBytes == null || audioBytes.length == 0) continue;
            audioPlayer.play(audioBytes);
        }
    }


    public void processImageFeed(List<FeedModel> models) {
        if (models.isEmpty() || stopImageProcessing.get()) return;

        long firstTimestamp = models.get(0).getTimestamp();

        for (FeedModel model : models) {
            long delay = model.getTimestamp() - firstTimestamp;
            if (delay < 0) continue;

            synchronized (this) {
                if (imageScheduler.isShutdown()) {
                    imageScheduler = Executors.newSingleThreadScheduledExecutor();
                }
            }
            imageScheduler.schedule(() -> renderImage(model), delay, TimeUnit.MILLISECONDS);
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
                byte[] imageBytes = model.getBase64Bytes();
                if (imageBytes == null || imageBytes.length == 0) {
                    return;
                }
                textureRenderer.updateTexture(textureView, imageBytes);

            } catch (Exception e) {
                Log.e("ProcessFeed", "Error processing image feed", e);
            } finally {
                isProcessingImage.set(false);
            }

        });
    }

    private void startLogging() {
        logHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateListener("Image Packet Wasted: "+ (Packets.imagePacketReceived - Packets.imagePacketExecuted));
                updateListener("Audio Packet Wasted: "+ (Packets.audioPacketReceived - Packets.audioPacketExecuted));
                logHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
