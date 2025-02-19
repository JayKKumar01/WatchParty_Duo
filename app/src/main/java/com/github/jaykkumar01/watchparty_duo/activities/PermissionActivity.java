package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.interfaces.PermissionCodes;

public class PermissionActivity extends AppCompatActivity implements PermissionCodes{

    private TextView tvCameraPermission, tvMicPermission, tvNotificationPermission;
    private ImageView imgCameraStatus, imgMicStatus, imgNotificationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        tvCameraPermission = findViewById(R.id.tvCameraPermission);
        tvMicPermission = findViewById(R.id.tvMicPermission);
        tvNotificationPermission = findViewById(R.id.tvNotificationPermission);
        imgCameraStatus = findViewById(R.id.imgCameraStatus);
        imgMicStatus = findViewById(R.id.imgMicStatus);
        imgNotificationStatus = findViewById(R.id.imgNotificationStatus);

        // Check and update permissions UI
        updatePermissionUI();

        // Request permissions when clicking on each row
        findViewById(R.id.layoutCamera).setOnClickListener(v -> requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE));
        findViewById(R.id.layoutMic).setOnClickListener(v -> requestPermission(Manifest.permission.RECORD_AUDIO, MIC_PERMISSION_CODE));
        findViewById(R.id.layoutNotification).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_CODE);
            }
        });
    }

    private void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private void updatePermissionUI() {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasMicPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        // Update text views
        tvCameraPermission.setText(hasCameraPermission ? "Camera: Granted" : "Camera: Not Granted");
        tvMicPermission.setText(hasMicPermission ? "Microphone: Granted" : "Microphone: Not Granted");
        tvNotificationPermission.setText(hasNotificationPermission ? "Notifications: Granted" : "Notifications: Not Granted");

        // Update status icons
        imgCameraStatus.setImageResource(hasCameraPermission ? R.drawable.ic_check_green : R.drawable.ic_cross_red);
        imgMicStatus.setImageResource(hasMicPermission ? R.drawable.ic_check_green : R.drawable.ic_cross_red);
        imgNotificationStatus.setImageResource(hasNotificationPermission ? R.drawable.ic_check_green : R.drawable.ic_cross_red);

        if (hasCameraPermission && hasMicPermission && hasNotificationPermission){
//            Intent intent = new Intent(this,FeedActivity.class);
            Intent intent = new Intent(this,TestingActivity.class);
//            Intent intent = new Intent(this,PlayerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updatePermissionUI();
        }
    }
}
