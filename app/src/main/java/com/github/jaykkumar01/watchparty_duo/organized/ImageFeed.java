package com.github.jaykkumar01.watchparty_duo.organized;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jaykkumar01.watchparty_duo.listeners.ImageFeedListener;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.CameraUtil;

import java.util.Arrays;

public class ImageFeed implements ImageReader.OnImageAvailableListener {
    private final Context context;
    private final TextureView textureView;
    private final ImageFeedListener imageFeedListener;
    private final CameraModel cameraModel;
    private final CameraSessionManager sessionManager;
    private ImageReader imageReader;
    private final ImageProcessor imageProcessor;


    public ImageFeed(Context context, ImageFeedListener listener, TextureView textureView){
        this.context = context;
        this.textureView = textureView;
        this.imageFeedListener = listener;
        this.cameraModel = new CameraModel(context);

        updateListener("Output Sizes: "+Arrays.toString(cameraModel.getOutputSizes()));

        Size previewSize = CameraUtil.chooseOptimalSize(
                cameraModel.getOutputSizes(),
                AppData.IMAGE_HEIGHT
        );
        @SuppressLint("DefaultLocale")
        String format = String.format("Optimal Size: %s, Ratio: %.2f", previewSize.toString(), (float) previewSize.getWidth() / previewSize.getHeight());
        updateListener(format);

        setupImageReader(previewSize);

        // Handler for the main thread
        this.sessionManager = new CameraSessionManager(context, cameraModel,imageReader);
        this.imageProcessor = new ImageProcessor(context,cameraModel,listener);
    }

    public void initializeCamera(){
        updateListener("Ranges: "+ Arrays.toString(cameraModel.getFpsRanges()));
        updateListener("Optimal Range: "+ cameraModel.getOptimalFpsRange() + ", FPS: "+AppData.FPS);

        sessionManager.openCamera(this::handleCameraSession);
    }

    private void setupImageReader(Size previewSize) {
        imageReader = ImageReader.newInstance(
                previewSize.getWidth(),
                previewSize.getHeight(),
                ImageFormat.YUV_420_888,
                2
        );
        imageReader.setOnImageAvailableListener(this, null);
    }

    private void handleCameraSession(CameraCaptureSession session) {

        try {
            CaptureRequest request = CameraSessionHelper.createCaptureRequest(
                    session.getDevice(),
                    imageReader.getSurface(),
                    cameraModel.getOptimalFpsRange()
            );
            if (request == null){
                Toast.makeText(context, "Capture Build Failed!", Toast.LENGTH_SHORT).show();
                return;
            }
            session.setRepeatingRequest(request, null, null);
        } catch (CameraAccessException e) {
            imageFeedListener.onError("Session configuration failed: " + e.getMessage());
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        imageProcessor.processImage(reader, textureView);
    }

    public void releaseResources() {
        sessionManager.closeSession();
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void updateListener(String logMessage) {
        if (imageFeedListener != null){
            imageFeedListener.onUpdate(logMessage);
        }
    }
}
