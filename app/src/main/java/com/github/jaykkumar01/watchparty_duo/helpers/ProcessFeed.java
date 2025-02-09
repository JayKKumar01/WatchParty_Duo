package com.github.jaykkumar01.watchparty_duo.helpers;

import android.media.AudioTrack;
import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioConfig;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioPlayer;
import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.List;
import java.util.concurrent.Executors;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;

public class ProcessFeed {

    private final TextureView remoteFeedTextureView;
    private final FeedListener feedListener;
    private final AudioPlayer audioPlayer;

    public ProcessFeed(TextureView remoteFeedTextureView, FeedListener feedListener){
        this.remoteFeedTextureView = remoteFeedTextureView;
        this.feedListener = feedListener;
        this.audioPlayer = new AudioPlayer(feedListener);
    }
    public void process(List<FeedModel> models, int feedType) {

        switch (feedType) {
            case FeedType.IMAGE_FEED: processImageFeed(models);
                break;
            case FeedType.AUDIO_FEED: processAudioFeed(models);
                break;
        }

    }

    private void processAudioFeed(List<FeedModel> models) {

        for (FeedModel model: models){
            byte[] audioBytes = model.getBase64Bytes();
            if (audioBytes == null || audioBytes.length == 0){
                return;
            }
            audioPlayer.play(audioBytes);
        }
    }

    private void processImageFeed(List<FeedModel> models) {
        long prevTimestamp = 0;

        for (FeedModel model: models){
            try {
                long timestamp = model.getTimestamp();
                if (timestamp < prevTimestamp){
                    continue;
                }
                // Calculate delay based on timestamp difference
                if (prevTimestamp != 0) {
                    int delay = (int) (timestamp - prevTimestamp);
                    Thread.sleep(delay);
                }
                prevTimestamp = timestamp; // Update for next iteration

                byte[] imageBytes = model.getBase64Bytes();
                if (imageBytes == null || imageBytes.length == 0) {
                    return;
                }
                TextureRenderer.updateTexture(remoteFeedTextureView, imageBytes);
            } catch (Exception e) {
                Log.e("FeedProcessor", "Error processing Image feed item", e);
            }
        }
    }

    private void updateListener(String logMessage) {
        if (feedListener != null){
            feedListener.onUpdate(logMessage);
        }
    }
}
