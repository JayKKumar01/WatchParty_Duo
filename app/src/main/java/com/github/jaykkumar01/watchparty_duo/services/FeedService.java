package com.github.jaykkumar01.watchparty_duo.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.constants.FeedServiceInfo;
import com.github.jaykkumar01.watchparty_duo.feed.FeedActivity;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;

import java.lang.ref.WeakReference;

public class FeedService extends Service{

    private static WeakReference<FeedService> instanceRef;

    public static FeedService getInstance() {
        return instanceRef != null ? instanceRef.get() : null;
    }
    private FeedManager feedManager;


    @Override
    public void onCreate() {
        super.onCreate();
        instanceRef = new WeakReference<>(this);
        feedManager = new FeedManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FeedServiceInfo.NOTIFICATION_ID, createNotification(false));
        feedManager.startWebFeed();
        return START_NOT_STICKY;
    }


    public void connect(String remoteId) {
        feedManager.connect(remoteId);
    }






    private Notification createNotification(boolean isConnectionOpen) {
        return new NotificationCompat.Builder(this, FeedServiceInfo.CHANNEL_ID)
                .setSmallIcon(R.drawable.mic_on)
                .setContentTitle("WatchParty")
                .setContentText(isConnectionOpen
                        ? FeedServiceInfo.NOTIFICATION_DESCRIPTION_CONNECTED
                        : FeedServiceInfo.NOTIFICATION_DESCRIPTION_NOT_CONNECTED)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .build();
    }

    public void updateNotification(boolean isConnectionOpen) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(FeedServiceInfo.NOTIFICATION_ID, createNotification(isConnectionOpen));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        RefHelper.reset(instanceRef);
        feedManager.stopFeeds();

        // Cancel the foreground notification
        stopForeground(true);

        // Cancel any remaining notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(FeedServiceInfo.NOTIFICATION_ID);

        super.onDestroy();
    }





}