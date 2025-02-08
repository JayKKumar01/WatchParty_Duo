package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import android.util.Range;
import android.view.Surface;

public class CameraSessionHelper {
    public static CaptureRequest createCaptureRequest(CameraDevice device, Surface surface, Range<Integer> optimalFpsRange) {
        try {
            CaptureRequest.Builder captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(surface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, optimalFpsRange);
            return captureBuilder.build();
        } catch (CameraAccessException e) {
            Log.e("ImageFeed", "Capture session setup failed", e);
        }
        return null;
    }
}
