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

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessFeed {
    private TextureView textureView;
    private final AudioPlayer audioPlayer;
    private ScheduledExecutorService imageScheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService imageProcessingExecutor = Executors.newCachedThreadPool();

    private final AtomicBoolean isProcessingImage = new AtomicBoolean(false);
    private final AtomicBoolean stopImageProcessing = new AtomicBoolean(false);
    private final TextureRenderer textureRenderer;
    private final Handler logHandler = new Handler(Looper.getMainLooper());


    public ProcessFeed() {
        this.audioPlayer = new AudioPlayer();
        this.textureRenderer = new TextureRenderer(false);
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
    }

    public void stopImageProcess() {
        stopImageProcessing.set(true);
        if (imageScheduler != null && !imageScheduler.isShutdown()){
            imageScheduler.shutdownNow();
        }
        if (imageProcessingExecutor != null && !imageProcessingExecutor.isShutdown()){
            imageProcessingExecutor.shutdownNow();
        }
    }

    public void processAudioFeed(List<FeedModel> models) {
        for (FeedModel model : models) {
            String base64 = model.getAudioFeedModel().getBase64Data();
            byte[] audioBytes = Base64.decode(base64,Base64.NO_WRAP);
            if (audioBytes == null || audioBytes.length == 0) continue;
            audioPlayer.play(audioBytes);
        }
    }

    private long firstPacketTime = 0, firstArrival = 0, finalArrival = 0, averageInterval = 0;
    private int packetNumber = 0;


    public void processImageFeed(List<FeedModel> models) {
        if (models.isEmpty() || stopImageProcessing.get()) return;

        long currentTime = System.currentTimeMillis();

        // Only synchronize necessary operations
        synchronized (this) {
            if (firstPacketTime == 0 && firstArrival == 0) {
                firstPacketTime = models.get(0).getTimestamp(); // Set first packet timestamp
                firstArrival = currentTime; // Set initial arrival time
                finalArrival = firstArrival; // Initialize final arrival
            }
            packetNumber++;

            if (packetNumber <= (1000/Feed.LATENCY)) {
                // Efficient O(1) update for average interval
                averageInterval += (currentTime - finalArrival - averageInterval) / packetNumber;
                // Correct finalArrival calculation
                finalArrival = firstArrival + averageInterval;
            }
        }

        long actualArrival = currentTime - finalArrival;

        for (FeedModel model : models) {
            long expectedArrival = model.getTimestamp() - firstPacketTime;
            long scheduleAfter = expectedArrival - actualArrival;

            if (scheduleAfter < 0){
                continue;
            }

            // Synchronize only if the scheduler needs to be restarted
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
            } finally {
                isProcessingImage.set(false);
            }

        });
    }

    public void stop(){
        stopImageProcess();
    }
}
