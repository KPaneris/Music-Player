package org.example.demo1;

import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Audius_API {

    private static final String BASE_URL = "https://api.audius.co/v1";
    private static final String CONTENT_GATEWAY = "https://audius-content-10.cultur3stake.com/content/";

    /**
     * Fetches trending tracks from the Audius API and returns a map of track titles to their streaming URLs.
     *
     * @return Map containing track titles as keys and their respective streaming URLs as values.
     * @throws Exception If there is an error during the API call or response parsing.
     */




    public static Map<String, String> fetchTrendingTracks() throws Exception {
        String endpoint = BASE_URL + "/tracks/trending";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parse the JSON response to extract track titles and streaming URLs
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray tracks = jsonResponse.getJSONArray("data");

            Map<String, String> trackMap = new HashMap<>();
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i);

                // Extract track data
                String title = track.optString("title", "Unknown Title");
                String trackId = track.optString("id", null); // Unique identifier
                String trackCid = track.optString("track_cid", null); // Use track_cid to fetch audio
                String streamUrl = null;

                if (trackCid != null) {
                    // Construct a direct streaming URL using the CID and content gateway
                    streamUrl = CONTENT_GATEWAY + trackCid;
                } else {
                    System.out.println("Track missing streaming CID: " + title);
                }

                // Use trackId as the unique key in the map
                if (trackId != null && streamUrl != null) {
                    trackMap.put(trackId, streamUrl);
                }
            }

            System.out.println("Fetched tracks: " + trackMap.size());
            return trackMap;
        } else {
            throw new Exception("Failed to fetch trending tracks. Response code: " + responseCode);
        }
    }

    /**
     * Fetches track details by ID.
     *
     * @param trackId The unique ID of the track.
     * @return Map containing track details (e.g., title, artist, streaming URL).
     * @throws Exception If there is an error during the API call or response parsing.
     */
    public static Map<String, String> fetchTrackById(String trackId) throws Exception {
        String endpoint = BASE_URL + "/tracks/" + trackId;
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parse the JSON response to extract track details
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject track = jsonResponse.getJSONObject("data");

            // Extract relevant details
            String title = track.optString("title", "Unknown Title");
            String artist = track.optJSONObject("user").optString("handle", "Unknown Artist");
            String trackCid = track.optString("track_cid", null);

            String streamUrl = (trackCid != null) ? CONTENT_GATEWAY + trackCid : null;

            Map<String, String> trackDetails = new HashMap<>();
            trackDetails.put("title", title);
            trackDetails.put("artist", artist);
            trackDetails.put("streamingUrl", streamUrl);

            return trackDetails;
        } else {
            throw new Exception("Failed to fetch track by ID. Response code: " + responseCode);
        }
    }
}
