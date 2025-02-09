package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.utils.CameraUtil;

import java.util.Arrays;
import java.util.Collections;

public class CameraSessionManager implements ImageReader.OnImageAvailableListener{
    private final Context context;
    private final CameraModel cameraModel;
    private final FeedListener feedListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SessionCallback sessionCallback;
    private CameraCaptureSession cameraCaptureSession;
    private final ImageProcessor imageProcessor;
    private ImageReader imageReader;

    public CameraSessionManager(Context context, CameraModel cameraModel, FeedListener feedListener, TextureView textureView) {
        this.context = context;
        this.cameraModel = cameraModel;
        this.feedListener = feedListener;
        this.imageProcessor = new ImageProcessor(context,cameraModel, feedListener,textureView);
        setupImageReader();
    }

    private void setupImageReader() {
        updateListener("Output Sizes: "+ Arrays.toString(cameraModel.getOutputSizes()));

        Size previewSize = CameraUtil.chooseOptimalSize(
                cameraModel.getOutputSizes(),
                Feed.IMAGE_HEIGHT
        );
        @SuppressLint("DefaultLocale")
        String format = String.format("Optimal Size: %s, Ratio: %.2f", previewSize.toString(), (float) previewSize.getWidth() / previewSize.getHeight());
        updateListener(format);
        imageReader = ImageReader.newInstance(
                previewSize.getWidth(),
                previewSize.getHeight(),
                ImageFormat.YUV_420_888,
                Feed.IMAGE_READER_BUFFER
        );
        imageReader.setOnImageAvailableListener(this, null);
    }

    public void openCamera(SessionCallback sessionCallback){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.sessionCallback = sessionCallback;
        try {
            cameraModel.getCameraManager().openCamera(cameraModel.getCameraId(),stateCallback,mainHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            try {
                cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        cameraCaptureSession = session;
                        sessionCallback.onSessionReady(session, imageReader.getSurface());
                        imageProcessor.startScheduler();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    }
                }, mainHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e("ImageFeed", "Camera error: " + error);
            camera.close();
        }
    };


    public void closeSession() {
        if (cameraCaptureSession != null){
            cameraCaptureSession.close();
        }
        imageProcessor.stopScheduler();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        imageProcessor.onProcessImage(reader);
    }

    public interface SessionCallback{
        void onSessionReady(CameraCaptureSession session, Surface surface);
    }

    private void updateListener(String logMessage) {
        if (feedListener != null){
            feedListener.onUpdate(logMessage);
        }
    }
}
