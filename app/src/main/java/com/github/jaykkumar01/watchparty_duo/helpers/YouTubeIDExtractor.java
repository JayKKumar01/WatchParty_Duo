package com.github.jaykkumar01.watchparty_duo.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeIDExtractor {

    /**
     * Extracts the YouTube video ID from a given URL.
     *
     * @param url The YouTube video URL.
     * @return The video ID if found, otherwise null.
     */
    public static String extractYouTubeVideoId(String url) {
        // Define regex pattern for different YouTube URL formats
        String regex = "(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|v/|.+/)|youtu\\.be/)([a-zA-Z0-9_-]{11})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        // Check if the URL matches the pattern
        if (matcher.find()) {
            return matcher.group(1); // Return the captured video ID
        } else {
            return null; // Return null if no video ID is found
        }
    }
}
