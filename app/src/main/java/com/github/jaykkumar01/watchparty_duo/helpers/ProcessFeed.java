package com.github.jaykkumar01.watchparty_duo.helpers;

import android.util.Log;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.List;
import java.util.concurrent.Executors;

public class ProcessFeed {

    private final TextureView remoteFeedTextureView;

    public ProcessFeed(TextureView remoteFeedTextureView){
        this.remoteFeedTextureView = remoteFeedTextureView;
    }
    public void process(List<FeedModel> models, int feedType) {

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

                switch (feedType) {
                    case FeedType.IMAGE_FEED: processImageFeed(model);
                    break;
                    case FeedType.AUDIO_FEED: processAudioFeed(model);
                    break;
                }
            } catch (Exception e) {
                Log.e("FeedProcessor", "Error processing 'Type "+feedType+"' feed item", e);
            }
        }

    }

    private void processAudioFeed(FeedModel model) {

    }

    private void processImageFeed(FeedModel model) {
        byte[] imageBytes = model.getBase64Bytes();
        if (imageBytes == null || imageBytes.length == 0) {
            return;
        }
        TextureRenderer.updateTexture(remoteFeedTextureView, imageBytes);
    }
}
