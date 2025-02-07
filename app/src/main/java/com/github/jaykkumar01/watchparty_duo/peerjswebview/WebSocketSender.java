package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.webkit.WebView;

import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.models.ImageFeedModel;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketSender {
    private ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Queue<ImageFeedModel> base64Queue = new ConcurrentLinkedQueue<ImageFeedModel>();
    private Context context;

    private UpdateListener updateListener;
    private final Gson gson = new Gson();

    private final Handler handler = new Handler();

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
                    synchronized (base64Queue) {
                        if (!base64Queue.isEmpty()) {
                            List<ImageFeedModel> batch = new ArrayList<>(base64Queue);
                            base64Queue.clear();
                            String json = gson.toJson(batch);

                            webView.post(() -> {
                                webView.evaluateJavascript(
                                        "receiveFromAndroid(" + json + ")",
                                        null
                                );
                            });
                        }
                    }
                },
                0,
                AppData.LATENCY_DELAY,
                TimeUnit.MILLISECONDS
        );
    }

    // Add image data with background conversion

    int minSize = Integer.MAX_VALUE;
    int maxSize = Integer.MIN_VALUE;
    public void addImageData(byte[] imageBytes, long timestamp) {
        Executors.newCachedThreadPool().execute(() -> {

            int lenKB = imageBytes.length/1024;
            if (lenKB < minSize || lenKB > maxSize){
                if (lenKB < minSize){
                    minSize = lenKB;
                }
                if (lenKB > maxSize){
                    maxSize = lenKB;
                }
                if (updateListener != null) {
                    updateListener.onUpdate("");
                    updateListener.onUpdate("Max Size: "+maxSize + " KB");
                    updateListener.onUpdate("Min Size: "+minSize + " KB");
                }
            }

            String base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            synchronized (base64Queue) {
                base64Queue.add(new ImageFeedModel(base64, timestamp));
            }
        });
    }

}
