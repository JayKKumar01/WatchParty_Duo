package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.converters.YUVConverter;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageProcessor {
    private static final int MAX_QUEUE_SIZE = 5; // Keep only latest frames
    private final Context context;
    private final CameraModel cameraModel;
    private final FeedListener feedListener;
    private final long frameIntervalMs;
    private final TextureView textureView;

    private int displayRotation = -1;
    private final Matrix rotationMatrix = new Matrix();
    private ScheduledExecutorService scheduler;
    private final ConcurrentLinkedQueue<FeedModel> imageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public ImageProcessor(Context context, CameraModel cameraModel, FeedListener feedListener, TextureView textureView) {
        this.context = context;
        this.cameraModel = cameraModel;
        this.feedListener = feedListener;
        this.textureView = textureView;
        this.frameIntervalMs = (long) (1000.0 / Feed.FPS);
    }

    public synchronized void startScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        updateListener("FPS: " + Feed.FPS + ", Frame Interval: " + frameIntervalMs);
        scheduler.scheduleWithFixedDelay(() -> {
            if (isProcessing.compareAndSet(false, true)) {
                try {
                    processImage();
                } finally {
                    isProcessing.set(false);
                }
            }
        }, 0, frameIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void onProcessImage(ImageReader reader) {
        Executors.newCachedThreadPool().execute(() -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image == null) return;

                byte[] bytes = YUVConverter.toJpegImage(image, 80);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);
                if (finalBitmap != bitmap) {
                    bitmap.recycle();
                }
                byte[] finalBytes = BitmapUtils.getBytes(finalBitmap);
                finalBitmap.recycle();

                // Maintain a fixed queue size (drop oldest if full)
                if (imageQueue.size() >= MAX_QUEUE_SIZE) {
                    imageQueue.poll();
                }
                imageQueue.offer(new FeedModel(finalBytes, System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    updateListener("Scheduler termination timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        imageQueue.clear();
    }

    private void processImage() {
        int size = imageQueue.size();
        if (size == 0) return;

        int middleIndex = size / 2;
        int currentIndex = 0;
        FeedModel middleModel = null;

        while (!imageQueue.isEmpty()) {
            FeedModel model = imageQueue.poll();
            if (currentIndex == middleIndex) {
                middleModel = model;
            }
            currentIndex++;
        }

        if (middleModel == null) return;

        byte[] bytes = middleModel.getBytes();
        long timeStamp = middleModel.getTimestamp();

        if (feedListener != null) {
            feedListener.onFeed(bytes, timeStamp, FeedType.IMAGE_FEED);
        }
        if (textureView != null) {
            TextureRenderer.updateTexture(textureView, bytes);
        }
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        if (bitmap == null) return null;

        int displayRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        if (this.displayRotation != displayRotation) {
            this.displayRotation = displayRotation;
            updateListener("Display Rotation: " + displayRotation);

            rotationMatrix.reset();
            int rotationDegrees = 0;
            switch (displayRotation) {
                case Surface.ROTATION_90: rotationDegrees = 270; break;
                case Surface.ROTATION_180: rotationDegrees = 180; break;
                case Surface.ROTATION_270: rotationDegrees = 90; break;
                case Surface.ROTATION_0:
                    break;
            }

            int finalRotation = (cameraModel.getSensorOrientation() - rotationDegrees + 360) % 360;
            rotationMatrix.postRotate(finalRotation);
        }

        Matrix tempMatrix = new Matrix(rotationMatrix);
        tempMatrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), tempMatrix, true);
    }

    private void updateListener(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}
