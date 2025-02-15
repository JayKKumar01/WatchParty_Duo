package com.github.jaykkumar01.watchparty_duo.helpers;

import android.util.Base64;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageFeedProcessor {
    private final PriorityBlockingQueue<FeedModel> frameQueue = new PriorityBlockingQueue<>(
            100, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()) // Sort by timestamp
    );
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);
    private ExecutorService imageProcessingExecutor = Executors.newCachedThreadPool();
    private Thread processingThread;
    private long firstPacketTime = 0;
    private long firstArrival = 0;

    private final FeedManager feedManager;
    private final TextureRenderer textureRenderer;
    private final TextureView textureView;

    public ImageFeedProcessor(FeedListener feedListener, FeedManager feedManager, TextureView textureView){
        this.feedManager = feedManager;
        this.textureRenderer = new TextureRenderer(false);
        this.textureView = textureView;
    }

    public void processImageFeed(List<FeedModel> models) {
        if (models.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        long packetTime = models.get(0).getTimestamp();

        synchronized (this) {
            if (firstPacketTime == 0) {
                firstPacketTime = packetTime;
                firstArrival = currentTime;
            }
        }

        long expectedDelay = packetTime - firstPacketTime;
        long actualDelay = currentTime - firstArrival;
        long packetDelay = actualDelay - expectedDelay;

        feedManager.onUpdate("\nPacket Delay: [" + actualDelay + "," + expectedDelay + "] = " + packetDelay);

        for (FeedModel model : models) {
            long delay = model.getTimestamp() - firstPacketTime - packetDelay;
            feedManager.onUpdate("Frame Schedule: [" + delay + "," + (model.getTimestamp() - firstPacketTime) + "]");

            frameQueue.offer(model); // Add frame to queue
        }

        startProcessingThread();
    }

    private void startProcessingThread() {
        if (isRunning.get()) return;

        isRunning.set(true);
        processingThread = new Thread(() -> {
            while (isRunning.get()) {
                try {
                    FeedModel model = frameQueue.take(); // Take the next frame
                    long renderTime = model.getTimestamp() - firstPacketTime;

                    long currentTime = System.currentTimeMillis() - firstArrival;
                    long sleepTime = renderTime - currentTime;

                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime); // Sleep until it's time to render
                    }

                    renderImage(model);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        processingThread.start();
    }

    public void stopProcessing() {
        isRunning.set(false);
        if (processingThread != null) {
            processingThread.interrupt();
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
}

