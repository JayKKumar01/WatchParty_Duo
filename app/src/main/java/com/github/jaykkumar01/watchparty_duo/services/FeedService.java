package com.github.jaykkumar01.watchparty_duo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.activities.PlayerActivity;
import com.github.jaykkumar01.watchparty_duo.helpers.FeedNotificationHelper;
import com.github.jaykkumar01.watchparty_duo.constants.FeedServiceInfo;
import com.github.jaykkumar01.watchparty_duo.helpers.RefHelper;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.ForegroundNotifier;
import com.github.jaykkumar01.watchparty_duo.managers.FeedManager;

import java.lang.ref.WeakReference;

public class FeedService extends Service implements ForegroundNotifier {

    private static WeakReference<FeedService> instanceRef;
    private FeedManager feedManager;
    private FeedNotificationHelper notificationHelper;

    public static FeedService getInstance() {
        return instanceRef != null ? instanceRef.get() : null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instanceRef = new WeakReference<>(this);
        feedManager = new FeedManager(this,this);
        notificationHelper = new FeedNotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FeedServiceInfo.NOTIFICATION_ID, notificationHelper.createNotification(false));
        feedManager.startWebFeed();
        return START_NOT_STICKY;
    }

    public void connect(String remoteId) {
        feedManager.connect(remoteId);
    }

    public void isVideo(boolean isVideo) {
        feedManager.isVideo(isVideo);
    }

    public void muteAudio(boolean mute){
        feedManager.muteAudio(mute);
    }

    public void deafenAudio(boolean isDeafen) {
        feedManager.deafenAudio(isDeafen);
    }

    public void stopService() {
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        RefHelper.reset(instanceRef);
        feedManager.destroy();
        notificationHelper.cancelNotification();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void updateNotification(boolean isConnectionOpen) {
        notificationHelper.updateNotification(isConnectionOpen);
    }

    @Override
    public void onUpdateLogs(String logMessage) {
        PlayerActivity playerActivity = PlayerActivity.getInstance();
        if (playerActivity != null){
            playerActivity.addLog(logMessage);
        }
    }

    @Override
    public void onConnectionClosed() {
        PlayerActivity playerActivity = PlayerActivity.getInstance();
        if (playerActivity != null){
            playerActivity.onConnectionClosed();
        }
    }

    public void setFeedSurfaces(TextureView peerFeed, TextureView remoteFeed) {
        feedManager.setFeedSurfaces(peerFeed,remoteFeed);
    }

    public FeedListener getFeedListener() {
        return feedManager;
    }

    public void onActivityStateChanged(boolean isRestarting, boolean isVideo) {
        feedManager.onActivityStateChanged(isRestarting,isVideo);
    }


}
