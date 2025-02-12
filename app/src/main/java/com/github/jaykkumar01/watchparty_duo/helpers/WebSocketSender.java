package com.github.jaykkumar01.watchparty_duo.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.listeners.ForegroundNotifier;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.models.FeedSizeTracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketSender {
    private ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Queue<FeedModel> base64Queue = new ConcurrentLinkedQueue<FeedModel>();
    private Context context;
    private ForegroundNotifier foregroundNotifier;
    private final Gson gson = new Gson();
    private final FeedSizeTracker feedSizeTracker = new FeedSizeTracker(); // Instance of tracker
    private ExecutorService dataExecutor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public WebSocketSender(Context context, ForegroundNotifier foregroundNotifier) {
        this.context = context;
        this.foregroundNotifier = foregroundNotifier;
    }

    public void setForegroundNotifier(ForegroundNotifier foregroundNotifier) {
        this.foregroundNotifier = foregroundNotifier;
    }

    public void initializeSender(WebView webView) {
        if (senderExecutor.isShutdown()){
            senderExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        // Schedule batch sender every 333ms
        senderExecutor.scheduleWithFixedDelay(
                () -> {
                    if (!base64Queue.isEmpty()) {

                        List<FeedModel> batch = new ArrayList<>(base64Queue);
                        base64Queue.clear();
                        String json = gson.toJson(batch);

                        // Post execution on the main thread using Handler
                        mainHandler.post(() -> {
                            if (webView != null){
                                webView.loadUrl("javascript:receiveFromAndroid(" + json + ")");
                            }
                        });
                    }
                },
                0,
                Feed.LATENCY,
                TimeUnit.MILLISECONDS
        );
    }

    // Add data with background conversion
    public void addData(byte[] bytes, long timestamp, int feedType) {
        if (dataExecutor.isShutdown()){
            dataExecutor = Executors.newCachedThreadPool();
        }
        dataExecutor.execute(() -> {

            int lenKB = bytes.length / 1024;

            // Update size tracking
            if (feedSizeTracker.updateSize(lenKB, feedType)){
                if (foregroundNotifier != null){
                    foregroundNotifier.onUpdateLogs(feedSizeTracker.toString());
                }
            }

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            FeedModel feedModel = new FeedModel(base64, timestamp);
            feedModel.setFeedType(feedType);
            base64Queue.add(feedModel);
        });
    }

    public void addSignalData(FeedModel feedModel) {
        base64Queue.add(feedModel);
    }
}
