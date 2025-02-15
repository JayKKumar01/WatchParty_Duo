package com.github.jaykkumar01.watchparty_duo.helpers;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.Metadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MetadataHelper {

    private static final Gson gson = new Gson();

    public static void set(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        // Convert JSON string to a Map
        Map<String, Integer> map = gson.fromJson(
                jsonData,
                new TypeToken<Map<String, Integer>>() {}.getType()
        );

        // Extract values safely
        Integer latency = map.get(Metadata.LATENCY);
        Integer resolution = map.get(Metadata.RESOLUTION);
        Integer fps = map.get(Metadata.FPS);

        // Assign values to Feed class
        if (latency != null) {
            Feed.LATENCY = latency;
        }
        if (resolution != null) {
            Feed.RESOLUTION = resolution;
        }
        if (fps != null) {
            Feed.FPS = fps;
        }
    }
}