package com.github.jaykkumar01.watchparty_duo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionHandler {

    public static void requestPermission(Activity activity,String permission,int code) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
    }

    public static boolean hasPermission(Activity activity) {
        // Check if all permissions are granted
        boolean hasNotificationPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        boolean hasCameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasMicPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        // If any of the permissions are not granted, request them
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            requestPermission(activity,Manifest.permission.POST_NOTIFICATIONS, Constants.NOTIFICATION_PERMISSION_CODE);
        }

        if (!hasCameraPermission) {
            requestPermission(activity,Manifest.permission.CAMERA, Constants.CAMERA_PERMISSION_CODE);
        }
        if (!hasMicPermission) {
            requestPermission(activity,Manifest.permission.RECORD_AUDIO, Constants.MIC_PERMISSION_CODE);
        }

        return hasNotificationPermission && hasCameraPermission && hasMicPermission;
    }
}
