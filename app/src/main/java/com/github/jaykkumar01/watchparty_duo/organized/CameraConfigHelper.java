package com.github.jaykkumar01.watchparty_duo.organized;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Range;
import android.util.Size;

import com.github.jaykkumar01.watchparty_duo.helpers.RangeCalculator;

public class CameraConfigHelper {
    public static String findFrontCameraId(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Integer cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (cameraFacing != null && cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId;
            }
        }
        throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "No front camera found");
    }

    public static int getSensorOrientation(CameraCharacteristics characteristics) {
        Integer cameraRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return cameraRotation == null ? 0 : cameraRotation;
    }

    public static Range<Integer> getOptimalFpsRange(Range<Integer>[] ranges, int fps) {
        return RangeCalculator.getOptimalRange(ranges, fps);
    }

    public static Size[] getOutputSizes(CameraCharacteristics characteristics, int format) {
        StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (configMap != null) {
            return configMap.getOutputSizes(format);
        }
        throw new IllegalArgumentException("StreamConfigurationMap for output sizes is not available");
    }

}
