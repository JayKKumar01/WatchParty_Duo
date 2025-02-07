package com.github.jaykkumar01.watchparty_duo.models;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.helpers.RangeCalculator;
import com.github.jaykkumar01.watchparty_duo.transferfeeds.ImageFeed;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;

import java.util.Collections;

public class CameraModel {
    private final Context context;

    private final CameraManager cameraManager;
    private String cameraId;
    private int cameraRotation;

    private int imageFormat = ImageFormat.YUV_420_888;

    private CameraCharacteristics characteristics;
    private Range<Integer>[] fpsRanges;
    private Range<Integer> optimalFpsRange;
    private StreamConfigurationMap configMap;
    private Size[] outputSizes;
    private CameraCaptureSession cameraCaptureSession;

    public CameraModel(Context context) throws CameraAccessException {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initialize();
    }
    private void initialize() throws CameraAccessException {
        for (String cameraId : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                this.cameraId = cameraId;
                Integer cameraRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                this.cameraRotation = cameraRotation == null ? 0 : cameraRotation;
            }
        }
        characteristics = cameraManager.getCameraCharacteristics(cameraId);
        fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        optimalFpsRange = RangeCalculator.getOptimalRange(fpsRanges, AppData.FPS);
        configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (configMap != null) {
            outputSizes = configMap.getOutputSizes(getImageFormat());
        }
    }

    public int getImageFormat() {
        return imageFormat;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public String getCameraId() {
        return cameraId;
    }

    public int getCameraRotation() {
        return cameraRotation;
    }

    public CameraCharacteristics getCharacteristics() {
        return characteristics;
    }

    public Range<Integer>[] getFpsRanges() {
        return fpsRanges;
    }

    public Range<Integer> getOptimalFpsRange() {
        return optimalFpsRange;
    }

    public StreamConfigurationMap getConfigMap() {
        return configMap;
    }

    public Size[] getOutputSizes() {
        return outputSizes;
    }

    public void setImageFormat(int imageFormat) {
        this.imageFormat = imageFormat;
    }

    public CameraCaptureSession getCameraCaptureSession() {
        return cameraCaptureSession;
    }

    public void openCamera(CameraDevice.StateCallback stateCallback, Handler mainHandler) throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, mainHandler);
    }

    public void createCaptureSession(CameraDevice cameraDevice, ImageReader imageReader, Handler mainHandler) {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    cameraCaptureSession = session;

                    try {
                        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureBuilder.addTarget(imageReader.getSurface());
                        captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

                        captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getOptimalFpsRange());

                        session.setRepeatingRequest(captureBuilder.build(), null, mainHandler);
                    } catch (CameraAccessException e) {
                        Log.e("ImageFeed", "Capture session setup failed", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}

