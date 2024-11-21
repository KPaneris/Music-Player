package org.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;


public class MusicPlayerController {

    @FXML
    public Button artist, playlist, mood, settings, love, home;
    @FXML
    public AnchorPane FrameMusicPlayer;
    @FXML
    private TextField searchBar; // Input field for search text.
    @FXML
    private ListView<String> resultsList; // ListView to display search results.
    @FXML
    private MediaPlayer mediaPlayer;  // MediaPlayer to handle audio playback
    @FXML
    private MediaView mediaView;  // MediaView to display the audio (optional if you're only focusing on audio)


    // Initialize trackMap as an empty HashMap
    private Map<String, String> trackMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Ensure ListView is interactive
        resultsList.setVisible(false); // Initially hidden
        resultsList.setOnMouseClicked(this::handleListClick); // Attach click listener

        // Fetch trending tracks when the application starts
        fetchTrendingTracks();
    }

    private void fetchTrendingTracks() {
        // Fetch trending tracks in a separate thread to avoid blocking the UI
        new Thread(() -> {
            try {
                String apiUrl = "https://discoveryprovider.audius.co/v1/tracks/search"; // Ensure this is correct
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {  // 200 OK
                    Platform.runLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error: Received HTTP " + responseCode + " from Audius API.", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }

                // Read the API response
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder jsonBuilder = new StringBuilder();
                while (scanner.hasNext()) {
                    jsonBuilder.append(scanner.nextLine());
                }
                scanner.close();

                String response = jsonBuilder.toString();
                System.out.println("Raw response: " + response);

                if (response.trim().startsWith("{")) {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray tracks = jsonResponse.getJSONArray("data");

                    Platform.runLater(() -> {
                        trackMap.clear(); // Clear previous mappings
                        for (int i = 0; i < tracks.length(); i++) {
                            JSONObject track = tracks.getJSONObject(i);
                            String title = track.getString("title");
                            String artist = track.getJSONObject("user").getString("name");
                            String trackId = track.getString("id"); // Extract trackId

                            trackMap.put(title + " by " + artist, trackId); // Store trackId
                            resultsList.getItems().add(title + " by " + artist);
                        }
                        resultsList.setVisible(true); // Display results
                    });
                } else {
                    Platform.runLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error: Invalid response from API. Response was not JSON.", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    JOptionPane.showMessageDialog(null, "Error fetching trending tracks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchBar.getText(); // Get search text
        if (query == null || query.isEmpty()) {
            resultsList.getItems().clear();
            resultsList.getItems().add("Please enter a search term.");
            return;
        }

        resultsList.getItems().clear(); // Clear old results
        new Thread(() -> {
            try {
                String apiUrl = "https://discoveryprovider.audius.co/v1/tracks/search?query=" + query;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                Scanner scanner = new Scanner(url.openStream());
                StringBuilder jsonBuilder = new StringBuilder();
                while (scanner.hasNext()) {
                    jsonBuilder.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
                JSONArray tracks = jsonResponse.getJSONArray("data");

                Platform.runLater(() -> {
                    trackMap.clear(); // Clear previous mappings
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject track = tracks.getJSONObject(i);
                        String title = track.getString("title");
                        String artist = track.getJSONObject("user").getString("name");
                        String trackId = track.getString("id"); // Extract trackId

                        trackMap.put(title + " by " + artist, trackId);
                        resultsList.getItems().add(title + " by " + artist);
                    }
                    resultsList.setVisible(true); // Display results
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    JOptionPane.showMessageDialog(null, "Error: Unable to fetch search results.", "Error", JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 1) { // Single-click
            String selectedTrack = resultsList.getSelectionModel().getSelectedItem();
            if (selectedTrack != null) {
                String trackId = trackMap.get(selectedTrack); // Retrieve trackId from trackMap
                if (trackId != null) {
                    // Call getStreamingUrl to get the actual streaming URL
                    String trackUrl = AudiusPlayer.getStreamingUrl(trackId); // Use the new getStreamingUrl method

                    if (trackUrl == null || trackUrl.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    System.out.println("Playing: " + trackUrl);

                    // Now play the track using the correct streaming URL
                    new Thread(() -> {
                        try {
                            AudiusPlayer.playAudioFromUrl(trackUrl); // Play the track
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                JOptionPane.showMessageDialog(null, "Error: Unable to play track.", "Error", JOptionPane.ERROR_MESSAGE);
                            });
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
