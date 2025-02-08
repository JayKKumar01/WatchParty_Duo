package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.util.Range;
import android.util.Size;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;

public class CameraModel {
    private final CameraManager cameraManager;
    private final String cameraId;
    private final int sensorOrientation;
    private final Range<Integer> optimalFpsRange;
    private final Size[] outputSizes;
    private final Range<Integer>[] fpsRanges;

    public CameraModel(Context context){
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            this.cameraId = CameraConfigHelper.findFrontCameraId(cameraManager);
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        this.sensorOrientation = CameraConfigHelper.getSensorOrientation(characteristics);
        this.fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        this.optimalFpsRange = CameraConfigHelper.getOptimalFpsRange(fpsRanges, AppData.FPS);
        this.outputSizes = CameraConfigHelper.getOutputSizes(characteristics,ImageFormat.YUV_420_888);
    }

    // Getters
    public CameraManager getCameraManager() { return cameraManager; }
    public String getCameraId() { return cameraId; }
    public int getSensorOrientation() { return sensorOrientation; }

    public Range<Integer>[] getFpsRanges() {
        return fpsRanges;
    }

    public Range<Integer> getOptimalFpsRange() { return optimalFpsRange; }
    public Size[] getOutputSizes() { return outputSizes; }
}
