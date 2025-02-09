package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.models.FeedSizeTracker;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketSender {
    private ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Queue<FeedModel> base64Queue = new ConcurrentLinkedQueue<FeedModel>();
    private Context context;

    private UpdateListener updateListener;
    private final Gson gson = new Gson();
    private final FeedSizeTracker feedSizeTracker = new FeedSizeTracker(); // Instance of tracker
    private ExecutorService dataExecutor = Executors.newCachedThreadPool();

    public WebSocketSender(Context context) {
        this.context = context;
    }

    public void setUpdateListener(UpdateListener updateListener){
        this.updateListener = updateListener;
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

                        webView.post(() -> {
                            webView.evaluateJavascript(
                                    "receiveFromAndroid(" + json + ")",
                                    null
                            );
                        });
                    }
                },
                0,
                Feed.LATENCY_DELAY,
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
            if (feedSizeTracker.updateSize(lenKB, feedType) && updateListener != null){
                updateListener.onUpdate(feedSizeTracker.toString());
            }

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            FeedModel feedModel = new FeedModel(base64, timestamp);
            feedModel.setFeedType(feedType);
            base64Queue.add(feedModel);
        });
    }

}
