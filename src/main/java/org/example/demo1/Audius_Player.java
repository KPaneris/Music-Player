package org.example.demo1;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Audius_Player {

    /**
     * Play audio from a URL.
     */
    public static void playAudioFromUrl(String audioUrl) {
        new Thread(() -> {
            try {
                if (audioUrl == null || audioUrl.isEmpty()) {
                    System.err.println("Invalid audio URL: " + audioUrl);
                    return;
                }

                URL url = new URL(audioUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Failed to connect to URL: " + audioUrl + ". Response code: " + responseCode);
                    return;
                }

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                Player player = new Player(inputStream);
                player.play();
            } catch (Exception e) {
                System.err.println("Error playing audio from URL: " + audioUrl);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Fetch the streaming URL for a given track.
     * Uses a two-step process:
     * 1. Fetch metadata for the track to validate it exists.
     * 2. Retrieve the direct streaming URL if the track is valid.
     */
    public static String getStreamingUrl(String trackId) {
        try {
            if (trackId == null || trackId.isEmpty()) {
                System.err.println("Invalid track ID: " + trackId);
                return null;
            }

            // Step 1: Validate track metadata exists
            String metadataEndpoint = "https://audius-discovery-12.cultur3stake.com/v1/tracks/" + trackId;
            URL metadataUrl = new URL(metadataEndpoint);
            HttpURLConnection metadataConnection = (HttpURLConnection) metadataUrl.openConnection();
            metadataConnection.setRequestMethod("GET");
            metadataConnection.setRequestProperty("Accept", "application/json");
            metadataConnection.setConnectTimeout(5000);
            metadataConnection.setReadTimeout(5000);

            int metadataResponseCode = metadataConnection.getResponseCode();
            if (metadataResponseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to validate track metadata. Response code: " + metadataResponseCode);
                return null;
            }




            // If metadata is valid, proceed to fetch the streaming URL
            System.out.println("Track metadata validated for ID: " + trackId);

            // Step 2: Fetch the streaming URL
            String streamingEndpoint = "https://api.audius.co/v1/tracks/" + trackId + "/stream";
            URL streamingUrl = new URL(streamingEndpoint);
            HttpURLConnection streamingConnection = (HttpURLConnection) streamingUrl.openConnection();
            streamingConnection.setRequestMethod("GET");
            streamingConnection.setRequestProperty("Accept", "application/json");
            streamingConnection.setConnectTimeout(5000);
            streamingConnection.setReadTimeout(5000);

            int streamingResponseCode = streamingConnection.getResponseCode();
            if (streamingResponseCode == HttpURLConnection.HTTP_OK) {
                // Validate response URL
                String streamingUrlResponse = streamingConnection.getURL().toString();
                if (streamingUrlResponse != null && !streamingUrlResponse.isEmpty()) {
                    return streamingUrlResponse;
                } else {
                    System.err.println("Empty streaming URL returned for track ID: " + trackId);
                }
            } else {
                System.err.println("Failed to fetch streaming URL for track ID: " + trackId + ". Response code: " + streamingResponseCode);
            }
        } catch (Exception e) {
            System.err.println("Error fetching streaming URL for track ID: " + trackId);
            e.printStackTrace();
        }
        return null;
    }
}
