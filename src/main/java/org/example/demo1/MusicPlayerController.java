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
import java.util.*;
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
    private ComboBox<String> searchMode; // Dropdown for search mode
    @FXML
    private ListView<String> resultsList; // ListView to display search results.
    @FXML
    private MediaPlayer mediaPlayer;  // MediaPlayer to handle audio playback
    @FXML
    private MediaView mediaView;  // MediaView to display the audio (optional if you're only focusing on audio)
    @FXML
    private PlaylistItem lastSelectedSongMetadata;

    private MainApp mainApp;



    // Initialize trackMap as an empty HashMap
    private Map<String, String> trackMap = new HashMap<>();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
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


        // Set default search mode
        searchMode.setValue("Songs");
        searchMode.setOnAction(event -> {
            String selectedMode = searchMode.getValue();
            System.out.println("Search mode switched to: " + selectedMode);
        });


        // Add Enter key listener for searchBar
        searchBar.setOnKeyPressed(event -> {
                    if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                        handleSearch(); // Trigger search on Enter
                    }
                });


        /*XRISI TOU API KAI TIS KLASEIS Audius_Player*/
        // Ensure ListView is interactive
        resultsList.setVisible(false);
        resultsList.setOnMouseClicked(this::handleListClick);

        searchBar.setOnMouseClicked(event -> {
            resultsList.setVisible(true);
        });

        // Hide resultsList when clicking anywhere else in the window
        FrameMusicPlayer.setOnMouseClicked(event -> {
            if (!event.getTarget().equals(searchBar) && !event.getTarget().equals(resultsList)) {
                resultsList.setVisible(false);
            }
        });

        // Fetch trending tracks when the application starts
        fetchTracks();



        /*XRISI TOU API KAI TIS KLASEIS Audius_Player*/
    }


    @FXML
    private void handleLogoutAction() {
        try {
            // Navigate to the login page
            mainApp.showLoginPage();

            // Close the current window if it exists
            Stage currentStage = null;
            if (FrameMusicPlayer != null && FrameMusicPlayer.getScene() != null) {
                currentStage = (Stage) FrameMusicPlayer.getScene().getWindow();
            }

            if (currentStage != null) {
                currentStage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    private void fetchTracks() {
        new Thread(() -> {
            try {
                String apiUrl = "https://audius-discovery-12.cultur3stake.com/v1/tracks/search";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    showErrorMessage("Error: Received HTTP " + responseCode + " from Audius API.");
                    return;
                }

                String response = new String(connection.getInputStream().readAllBytes());
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray tracks = jsonResponse.getJSONArray("data");

                Platform.runLater(() -> {
                    System.out.println("Fetched tracks successfully.");
                    trackMap.clear();
                    resultsList.getItems().clear();
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject track = tracks.getJSONObject(i);
                        String title = track.getString("title");
                        String artistName = track.getJSONObject("user").getString("name");
                        String trackId = track.getString("id");

                        // Use the track_cid as a part of the response or for further URL construction
                        String trackCid = track.optString("track_cid", null);
                        if (trackCid != null && !trackCid.isEmpty()) {
                            String displayText = title + " by " + artistName;
                            trackMap.put(displayText, trackCid);
                            resultsList.getItems().add(displayText);
                        }
                    }
                    resultsList.setVisible(true);
                });

            } catch (Exception e) {
                showErrorMessage("Error fetching trending tracks: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }




    @FXML
    private void handleSearch() {
        String query = searchBar.getText();
        String selectedMode = searchMode.getValue();
        if (query == null || query.isEmpty()) {
            resultsList.getItems().clear();
            resultsList.getItems().add("Please enter a search term.");
            return;
        }

        resultsList.getItems().clear();
        new Thread(() -> {
            try {
                String apiUrl = buildApiUrl(query, selectedMode);
                String response = fetchDataFromApi(apiUrl);
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray dataArray = jsonResponse.getJSONArray("data");

                Platform.runLater(() -> {
                    System.out.println("Search results fetched successfully.");
                    trackMap.clear();
                    resultsList.getItems().clear();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        String displayText = buildDisplayText(selectedMode, item);
                        trackMap.put(displayText, item.getString("id"));
                        resultsList.getItems().add(displayText);
                    }
                    resultsList.setVisible(true);
                });

            } catch (Exception e) {
                showErrorMessage("Error: Unable to fetch search results.");
                e.printStackTrace();
            }
        }).start();
    }



    private String buildApiUrl(String query, String mode) {
        String baseUrl = "https://audius-discovery-12.cultur3stake.com/v1/";
        return switch (mode) {
            case "Artists" -> baseUrl + "users/search?query=" + query;
            case "Albums", "Playlists" -> baseUrl + "playlists/search?query=" + query;
            case "Songs" -> baseUrl + "tracks/search?query=" + query;
            default -> baseUrl + "tracks/search?query=" + query;
        };
    }

    private String fetchDataFromApi(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        return new String(connection.getInputStream().readAllBytes());
    }

    private String buildDisplayText(String mode, JSONObject item) {
        return switch (mode) {
            case "Artists" -> item.getString("name");
            case "Albums", "Playlists" -> item.getString("playlist_name") + " by " + item.getJSONObject("user").getString("name");
            default -> item.getString("title") + " by " + item.getJSONObject("user").getString("name");
        };
    }


    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }


    @FXML
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 1) { // Change this to 1 for single-click detection
            String selectedItem = resultsList.getSelectionModel().getSelectedItem();
            if (selectedItem != null && trackMap.containsKey(selectedItem)) {
                System.out.println("Item selected: " + selectedItem);
                handleItemSelection(selectedItem);
            } else {
                System.out.println("No item selected or item not found in trackMap.");
            }
        }
    }



    @FXML
    private void handleItemSelection(String selectedItem) {
        String trackCid = trackMap.get(selectedItem);
        if (trackCid != null) {
            System.out.println("Track selected: " + selectedItem);
            fetchTrackUrl(trackCid);  // Pass the CID directly
        } else {
            System.out.println("Track CID not found for selected item: " + selectedItem);
        }
    }


    @FXML
    private void fetchTrackUrl(String trackCid) {
        new Thread(() -> {
            try {
                // No change needed here if you already know that the trackCid is what you need.
                String trackUrl = "https://ipfs.io/ipfs/" + trackCid;

                Platform.runLater(() -> {
                    if (trackCid != null && !trackCid.isEmpty()) {
                        playTrack(trackUrl);
                    } else {
                        showErrorMessage("Error: No track CID found for the selected track.");
                    }
                });
            } catch (Exception e) {
                showErrorMessage("Error fetching track URL: " + e.getMessage());
                e.printStackTrace();

    @FXML
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 1) { // Single-click
            String selectedTrack = resultsList.getSelectionModel().getSelectedItem();

            if (selectedTrack != null) {
                String trackId = trackMap.get(selectedTrack); // Retrieve trackId from trackMap
                if (trackId != null) {
                    String trackUrl = Audius_Player.getStreamingUrl(trackId); // Retrieve streaming URL

                    // Extract metadata
                    String[] splitTrack = selectedTrack.split(" by ");
                    String title = (splitTrack.length > 0) ? splitTrack[0] : "Unknown Title";
                    String artist = (splitTrack.length > 1) ? splitTrack[1] : "Unknown Artist";

                    lastSelectedSongMetadata = new PlaylistItem(
                            title,
                            artist,
                            "Unknown Album",
                            "Unknown Duration",
                            trackUrl,
                            "No Thumbnail"
                    );

                    if (trackUrl == null || trackUrl.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    System.out.println("Opening Media Player for: " + title + " by " + artist);



                    // Hide the resultsList
                    Platform.runLater(() -> resultsList.setVisible(false));
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Track URL not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        }).start();
    }





    private void playTrack(String trackUrl) {
        try {
            Media media = new Media(trackUrl);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } catch (Exception e) {
            Platform.runLater(() -> {
                JOptionPane.showMessageDialog(null, "Error playing track: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }


    @FXML
    private void showLastSelectedMetadata() {
        if (lastSelectedSongMetadata != null) {
            System.out.println("Last Selected Metadata:\n" + lastSelectedSongMetadata);
        } else {
            System.out.println("No item selected.");
        }
    }


}