package com.github.jaykkumar01.watchparty_duo.helpers;

import android.util.Log;
import android.view.TextureView;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioPlayer;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessFeed {
    private final TextureView remoteFeedTextureView;
    private final FeedListener feedListener;
    private final AudioPlayer audioPlayer;
    private final ScheduledExecutorService imageScheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);

    public ProcessFeed(TextureView remoteFeedTextureView, FeedListener feedListener) {
        this.remoteFeedTextureView = remoteFeedTextureView;
        this.feedListener = feedListener;
        this.audioPlayer = new AudioPlayer(feedListener);
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
        updateListener("Image processing stopped");
    }

    public void process(List<FeedModel> models, int feedType) {
        if (models == null || models.isEmpty()) return;

        switch (feedType) {
            case FeedType.IMAGE_FEED:
                processImageFeed(models);
                break;
            case FeedType.AUDIO_FEED:
                processAudioFeed(models);
                break;
        }
    }

    private void processAudioFeed(List<FeedModel> models) {
        for (FeedModel model : models) {
            byte[] audioBytes = model.getBase64Bytes();
            if (audioBytes == null || audioBytes.length == 0) continue;
            audioPlayer.play(audioBytes);
        }
    }

    private void processImageFeed(List<FeedModel> models) {
        imageScheduler.execute(() -> {
            long prevTimestamp = 0;

            for (FeedModel model : models) {
                if (stopImageProcessing.get()) break; // Stop processing if requested

                if (isProcessingImage.get()) {
                    // Skip this model and move to the next
                    continue;
                }

                try {
                    isProcessingImage.set(true); // Mark image as being processed

                    long timestamp = model.getTimestamp();
                    if (timestamp < prevTimestamp) continue;

                    // Calculate delay based on timestamp
                    if (prevTimestamp != 0) {
                        long delay = timestamp - prevTimestamp;
                        TimeUnit.MILLISECONDS.sleep(delay);
                    }
                    prevTimestamp = timestamp;

                    byte[] imageBytes = model.getBase64Bytes();
                    if (imageBytes == null || imageBytes.length == 0) continue;

                    TextureRenderer.updateTexture(remoteFeedTextureView, imageBytes);
                } catch (Exception e) {
                    Log.e("ProcessFeed", "Error processing Image feed", e);
                } finally {
                    isProcessingImage.set(false); // Reset after processing
                }
            }
        });
    }

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
