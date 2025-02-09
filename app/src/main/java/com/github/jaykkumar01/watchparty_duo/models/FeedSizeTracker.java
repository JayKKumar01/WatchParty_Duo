package com.github.jaykkumar01.watchparty_duo.models;

import com.github.jaykkumar01.watchparty_duo.constants.FeedType;

public class FeedSizeTracker {
    private int minImageSize = Integer.MAX_VALUE;
    private int maxImageSize = Integer.MIN_VALUE;
    private int minAudioSize = Integer.MAX_VALUE;
    private int maxAudioSize = Integer.MIN_VALUE;

    /**
     * Updates the size and returns true if a change occurred.
     */
    public synchronized boolean updateSize(int sizeKB, int feedType) {
        boolean updated = false;

        if (feedType == FeedType.IMAGE_FEED) {
            if (sizeKB < minImageSize) {
                minImageSize = sizeKB;
                updated = true;
            }
            if (sizeKB > maxImageSize) {
                maxImageSize = sizeKB;
                updated = true;
            }
        } else if (feedType == FeedType.AUDIO_FEED) {
            if (sizeKB < minAudioSize) {
                minAudioSize = sizeKB;
                updated = true;
            }
            if (sizeKB > maxAudioSize) {
                maxAudioSize = sizeKB;
                updated = true;
            }
        }

        return updated; // Return true if a change occurred
    }

    public synchronized int getMinImageSize() {
        return minImageSize == Integer.MAX_VALUE ? 0 : minImageSize;
    }

    public synchronized int getMaxImageSize() {
        return maxImageSize == Integer.MIN_VALUE ? 0 : maxImageSize;
    }

    public synchronized int getMinAudioSize() {
        return minAudioSize == Integer.MAX_VALUE ? 0 : minAudioSize;
    }

    public synchronized int getMaxAudioSize() {
        return maxAudioSize == Integer.MIN_VALUE ? 0 : maxAudioSize;
    }

    @Override
    public String toString() {
        return "Image -> Min: " + getMinImageSize() + " KB, Max: " + getMaxImageSize() + " KB\n" +
                "Audio -> Min: " + getMinAudioSize() + " KB, Max: " + getMaxAudioSize() + " KB";
    }
}
