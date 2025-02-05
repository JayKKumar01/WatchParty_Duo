package com.github.jaykkumar01.watchparty_duo.services;


import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.transferfeeds.AudioFeed;
import com.github.jaykkumar01.watchparty_duo.utils.Constants;
import com.github.jaykkumar01.watchparty_duo.utils.WebViewUtil;


public class ConnectionService extends Service{

    private static ConnectionService instance;
    private WebViewUtil webViewUtil;
    private AudioFeed audioFeed;
    private final Handler handler = new Handler(Looper.getMainLooper());


    public static ConnectionService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        audioFeed = new AudioFeed(this);
        webViewUtil = new WebViewUtil(this);

        return START_NOT_STICKY;
    }





    private Notification createNotification() {

        return new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.mic_on)
                .setContentTitle("WatchParty")
                .setContentText(AppData.getInstance().isConnectionActive() ? Constants.NOTIFICATION_DESCRIPTION_CONNECTED :Constants.NOTIFICATION_DESCRIPTION_NOT_CONNECTED)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .build();
    }
    public void updateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(Constants.NOTIFICATION_ID, createNotification());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void connect(String remoteId) {
        webViewUtil.callJavaScript("connect", remoteId, true);
    }

    public void startAudioTransfer() {
        audioFeed.startRecording();
    }
    public void stopAudioTransfer() {
        audioFeed.stopRecording();
    }

    public void sendAudioFile(byte[] buffer, int read, long millis, float loudness) {
        webViewUtil.callJavaScript("sendAudioFile",buffer,read,millis,loudness);
    }

    public void toggleMic() {
        audioFeed.record(AppData.getInstance().isMute());
    }


    public void stop() {
        webViewUtil.stop();
        stopForeground(true); // Removes the notification
        stopSelf(); // Stops the service
    }

    public void showToast(String toast) {
        handler.post(() -> Toast.makeText(instance, toast, Toast.LENGTH_SHORT).show());


    }


    public void onPeerOpen(String peerId) {
        startForeground(Constants.NOTIFICATION_ID, createNotification());
    }

    public void sendImageFeed(byte[] imageFeedBytes, long millis) {
        webViewUtil.callJavaScript("onImageFeed",imageFeedBytes, millis);
    }
}
