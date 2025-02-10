package com.github.jaykkumar01.watchparty_duo.helpers;

import android.util.Log;
import android.view.TextureView;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioPlayer;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessFeed {
    private TextureView remoteFeedTextureView;
    private final FeedListener feedListener;
    private final AudioPlayer audioPlayer;
    private ScheduledExecutorService imageScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService imageProcessingExecutor = Executors.newCachedThreadPool();

    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);

    public ProcessFeed(FeedListener feedListener) {
        this.feedListener = feedListener;
        this.audioPlayer = new AudioPlayer(feedListener);
    }

    public void setRemoteFeedTextureView(TextureView remoteFeedTextureView){
        this.remoteFeedTextureView = remoteFeedTextureView;
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
        imageScheduler.shutdownNow();  // Cancel all scheduled tasks
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

        if (models.isEmpty() || stopImageProcessing.get() || remoteFeedTextureView == null) return;

        long firstTimestamp = models.get(0).getTimestamp(); // Reference timestamp

        for (FeedModel model : models) {
            long delay = model.getTimestamp() - firstTimestamp; // Calculate delay based on the first frame
            if (delay <= 0){
                continue;
            }

            synchronized (this) {  // Ensures only one thread checks/reinitializes at a time
                if (imageScheduler.isShutdown()) {
                    imageScheduler = Executors.newSingleThreadScheduledExecutor();
                }
            }
            imageScheduler.schedule(() -> renderImage(model), delay, TimeUnit.MILLISECONDS);
        }
    }

    private void renderImage(FeedModel model) {
        if (stopImageProcessing.get()) {
            return; // Drop frame if processing is stopped or another image is in process
        }

        synchronized (isProcessingImage) {
            if (isProcessingImage.get()) return; // Double-check within sync block
            isProcessingImage.set(true);
        }

        synchronized (this){
            if (imageProcessingExecutor.isShutdown()){
                imageProcessingExecutor = Executors.newCachedThreadPool();
            }
        }
        imageProcessingExecutor.execute(() -> {
            try {
                byte[] imageBytes = model.getBase64Bytes();
                if (imageBytes == null || imageBytes.length == 0) return;

                TextureRenderer.updateTexture(remoteFeedTextureView, imageBytes);
            } catch (Exception e) {
                Log.e("ProcessFeed", "Error processing image feed", e);
            } finally {
                isProcessingImage.set(false);
            }
        });
    }

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
