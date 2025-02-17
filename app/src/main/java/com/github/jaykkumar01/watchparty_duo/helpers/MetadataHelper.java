package com.github.jaykkumar01.watchparty_duo.helpers;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.models.Metadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MetadataHelper {

    private static final Gson gson = new Gson();

    public static void set(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        // Convert JSON string to a Map
        Metadata model = gson.fromJson(
                jsonData,
                new TypeToken<Metadata>() {}.getType()
        );
        Feed.LATENCY = model.getLatency();
        Feed.RESOLUTION = model.getResolution();
        Feed.FPS = model.getFps();
    }
}