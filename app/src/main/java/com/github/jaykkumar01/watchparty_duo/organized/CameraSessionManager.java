package com.github.jaykkumar01.watchparty_duo.organized;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Collections;

public class CameraSessionManager {
    private final Context context;
    private final CameraModel cameraModel;
    private final ImageReader imageReader;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SessionCallback sessionCallback;
    private CameraCaptureSession cameraCaptureSession;

    public CameraSessionManager(Context context, CameraModel cameraModel, ImageReader imageReader) {
        this.context = context;
        this.cameraModel = cameraModel;
        this.imageReader = imageReader;
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
                        sessionCallback.onSessionReady(session);
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
    }

    public interface SessionCallback{
        void onSessionReady(CameraCaptureSession session);
    }
}
