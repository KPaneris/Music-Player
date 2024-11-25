package org.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import static java.awt.SystemColor.text;

public class MusicPlayerController {

    @FXML
    public Button artist, playlist, mood, settings, love, home,list;
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

        setTooltipWithDelay(list, "Categories");

        setTooltipWithDelay(mood, "My Mood");

        setTooltipWithDelay(settings, "Settings");

        setTooltipWithDelay(home, "Home");

        setTooltipWithDelay(playlist, "My Playlist");

        setTooltipWithDelay(artist, "My favourite Artists");

        setTooltipWithDelay(love, "My favourite Songs");




        // Δημιουργία του ContextMenu
        ContextMenu settingsMenu = new ContextMenu();

        // Δημιουργία του item "Log Out"
        MenuItem logoutItem = new MenuItem("Log Out");
        logoutItem.setOnAction(event -> handleLogoutAction());  // Συνδέουμε την ενέργεια για Log Out

        // Προσθήκη του item στο ContextMenu
        settingsMenu.getItems().add(logoutItem);

        // Όταν πατιέται το κουμπί settings, εμφανίζεται το ContextMenu
        settings.setOnAction(event -> {
            if (!settingsMenu.isShowing()) {
                settingsMenu.show(settings, settings.getLayoutX(), settings.getLayoutY() + settings.getHeight());
            } else {
                settingsMenu.hide();
            }
        });



        /*XRISI TOU API KAI TIS KLASEIS Audius_Player*/
        // Ensure ListView is interactive
        resultsList.setVisible(false); // Initially hidden
        resultsList.setOnMouseClicked(this::handleListClick); // Attach click listener

        // Add key event listener to the searchBar
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                handleSearch(); // Call handleSearch when Enter is pressed
            }
        });

        // Show resultsList when clicking on the searchBar
        searchBar.setOnMouseClicked(event -> {
            resultsList.setVisible(true); // Show the results list when searchBar is clicked
        });

        // Hide resultsList when clicking anywhere else in the window
        FrameMusicPlayer.setOnMouseClicked(event -> {
            if (!event.getTarget().equals(searchBar) && !event.getTarget().equals(resultsList)) {
                resultsList.setVisible(false); // Hide the results list if the click is outside
            }
        });

        // Fetch trending tracks when the application starts
        fetchTrendingTracks();



        /*XRISI TOU API KAI TIS KLASEIS Audius_Player*/
    }


    //xrisi gia log out alla then exei teliosei
    @FXML
    private void handleLogoutAction() {
        // Κλείνει το παράθυρο του MusicPlayer
        Stage currentStage = (Stage) FrameMusicPlayer.getScene().getWindow();
        currentStage.close();


    }

    //einai gia na emfanizonte i simasia ton koumpion sto Music Player
    private void setTooltipWithDelay(Button button, String text) {
        Tooltip tooltip = new Tooltip(text);



        // Ρυθμίζουμε την καθυστέρηση εμφάνισης και απόκρυψης του tooltip

        tooltip.setShowDelay(javafx.util.Duration.millis(100));  // 100 ms για γρηγορότερη εμφάνιση

        tooltip.setHideDelay(javafx.util.Duration.millis(100));  // 100 ms για γρηγορότερη απόκρυψη



        // Εφαρμόζουμε το tooltip στο κουμπί


        button.setTooltip(tooltip);



    }

    public void fetchTrendingTracks() {
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
                    String trackUrl = Audius_Player.getStreamingUrl(trackId); // Use the new getStreamingUrl method

                    if (trackUrl == null || trackUrl.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    System.out.println("Playing: " + trackUrl);

                    // Now play the track using the correct streaming URL
                    new Thread(() -> {
                        try {
                            Audius_Player.playAudioFromUrl(trackUrl); // Play the track
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                JOptionPane.showMessageDialog(null, "Error: Unable to play track.", "Error", JOptionPane.ERROR_MESSAGE);
                            });
                            e.printStackTrace();
                        }
                    }).start();

                    // Hide the resultsList after selecting a track
                    Platform.runLater(() -> resultsList.setVisible(false));
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
