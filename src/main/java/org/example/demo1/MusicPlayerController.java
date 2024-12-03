package org.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.example.demo1.utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MusicPlayerController {
    @FXML private Button artist, playlist, mood, settings, love, home, list;
    @FXML private AnchorPane FrameMusicPlayer;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> searchMode;
    @FXML private ListView<String> resultsList;
    @FXML private MediaPlayer mediaPlayer;
    @FXML private PlaylistItem lastSelectedSongMetadata;


    private Map<String, ItemInfo> trackMap = new HashMap<>();

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
    @FXML
    private PlaylistItem lastSelectedSongMetadata;

    private MainApp mainApp;





    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    @FXML
    public void initialize() {
        configureTooltips();
        configureSearchBar();
        configureResultsList();
        configureSettingsMenu();
        fetchTracks();
    }

    private void configureTooltips() {
        setTooltipWithDelay(list, "Categories");
        setTooltipWithDelay(mood, "My Mood");
        setTooltipWithDelay(settings, "Settings");
        setTooltipWithDelay(home, "Home");
        setTooltipWithDelay(playlist, "My Playlist");
        setTooltipWithDelay(artist, "My favourite Artists");
        setTooltipWithDelay(love, "My favourite Songs");
    }

    private void setTooltipWithDelay(Button button, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setHideDelay(javafx.util.Duration.millis(100));
        button.setTooltip(tooltip);
    }

    private void configureSearchBar() {
        searchMode.setValue("Songs"); // Set a default value for the search mode
        searchMode.setOnAction(event ->
                System.out.println("Search mode switched to: " + searchMode.getValue())
        );
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                handleSearch();
            }
        });
    }

    private void configureResultsList() {
        resultsList.setVisible(false);
        resultsList.setOnMouseClicked(this::handleListClick);
        searchBar.setOnMouseClicked(event -> resultsList.setVisible(true));
        FrameMusicPlayer.setOnMouseClicked(event -> {
            if (!event.getTarget().equals(searchBar) && !event.getTarget().equals(resultsList)) {
                resultsList.setVisible(false);
            }
        });



        // Fetch trending tracks when the application starts
        fetchTrendingTracks();



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

    private void configureSettingsMenu() {
        // Example implementation, adjust as needed                              //empty, need fixing
        settings.setOnAction(event -> {
            System.out.println("update code here");
            // Add your settings menu logic here
        });
    }

    private void fetchTracks() {
        new Thread(() -> {
            try {
                String apiUrl = "https://audius-discovery-12.cultur3stake.com/v1/tracks/search";
                String response = ApiClient.fetchData(apiUrl);
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray tracks = jsonResponse.getJSONArray("data");

                Platform.runLater(() -> updateSearchResultsWithStreamUrl(tracks, "Songs"));
            } catch (Exception e) {
                showErrorMessage("Error fetching tracks: " + e.getMessage());
            }
        }).start();
    }

    private void updateSearchResultsWithStreamUrl(JSONArray dataArray, String mode) {
        trackMap.clear();
        resultsList.getItems().clear();

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.getJSONObject(i);
            String displayText = buildDisplayText(mode, item);
            String itemId = item.optString("id", null);

            if (itemId != null && !itemId.isEmpty()) {
                if (mode.equals("Songs")) {
                    // Create a stream URL for songs and add to trackMap
                    String streamUrl = "https://audius-discovery-12.cultur3stake.com/v1/tracks/" + itemId + "/stream";
                    trackMap.put(displayText, new ItemInfo(itemId, "track", null, streamUrl));
                    resultsList.getItems().add(displayText);
                } else {
                    // Set type based on the mode for artists, albums, and playlists
                    String itemType = switch (mode) {
                        case "Artists" -> "artist";
                        case "Albums" -> "album";
                        case "Playlists" -> "playlist";
                        default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
                    };
                    trackMap.put(displayText, new ItemInfo(itemId, itemType, null, null));
                    resultsList.getItems().add(displayText);
                }
            } else {
                System.err.println("Skipping item due to missing id.");
            }
        }
        resultsList.setVisible(true);
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
                String response = ApiClient.fetchData(apiUrl);
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray dataArray = jsonResponse.getJSONArray("data");

                Platform.runLater(() -> updateSearchResultsWithStreamUrl(dataArray, selectedMode));
            } catch (Exception e) {
                showErrorMessage("Error fetching search results: " + e.getMessage());
            }
        }).start();
    }

    private void playStreamUrl(String streamUrl) {
        Platform.runLater(() -> {                                                   //javafx ui connection here
            try {
                Media media = new Media(streamUrl);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            } catch (Exception e) {
                showErrorMessage("Error playing track: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    @FXML
    private void handleListClick(MouseEvent event) {

        if (event.getClickCount() == 1) {
            String selectedItem = resultsList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ItemInfo selectedItemInfo = trackMap.get(selectedItem);
                if (selectedItemInfo != null) {
                    if ("track".equals(selectedItemInfo.getType())) {
                        // Play the stream URL for tracks
                        playStreamUrl(selectedItemInfo.getTrackUrl());
                    } else {
                        // Handle other item types (e.g., fetch details)
                        handleItemSelection(selectedItem, selectedItemInfo);

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
                }
            }
        }
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private HttpURLConnection createConnection(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    public class ItemInfo {
        private String id;
        private String type;
        private String cid;
        private String trackUrl;

        public ItemInfo(String id, String type, String cid, String trackUrl) {
            this.id = id;
            this.type = type;
            this.cid = cid;
            this.trackUrl = trackUrl;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getCid() { return cid; }
        public String getTrackUrl() { return trackUrl; }
    }


    private String buildDisplayText(String mode, JSONObject item) {
        return switch (mode) {
            case "Artists" -> item.getString("name");
            case "Albums", "Playlists" -> item.getString("playlist_name") + " by " + item.getJSONObject("user").getString("name");
            default -> item.getString("title") + " by " + item.getJSONObject("user").getString("name");
        };
    }

    private String fetchDataFromApi(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return new String(connection.getInputStream().readAllBytes());
        } else {
            showErrorMessage("Error: Received HTTP " + connection.getResponseCode() + " from Audius API.");
            return null;
        }
    }

    private void handleItemSelection(String selectedItem, ItemInfo itemInfo) {
        if (itemInfo != null) {
            System.out.println("Item selected: " + selectedItem + " of type: " + itemInfo.getType());
            switch (itemInfo.getType()) {
                case "artist":
                    fetchArtistDetails(itemInfo.getId());
                    break;
                case "album":
                    fetchAlbumDetails(itemInfo.getId());
                    break;
                case "playlist":
                    fetchPlaylistDetails(itemInfo.getId());
                    break;
                default:
                    System.out.println("Unknown item type: " + itemInfo.getType());
                    showErrorMessage("Unknown item type: " + itemInfo.getType());
                    break;
            }
        } else {
            System.out.println("Item not found in trackMap.");
        }
    }

    private void fetchDetails(String apiUrl, String type) {
        new Thread(() -> {
            try {
                String response = fetchDataFromApi(apiUrl);
                if (response != null) {
                    JSONObject details = new JSONObject(response);
                    Platform.runLater(() -> {
                        System.out.println(type + " details:\n" + details.toString(2)); // Pretty print JSON with indentation
                    });

                    System.out.println("Opening Media Player for: " + title + " by " + artist);



                    // Hide the resultsList
                    Platform.runLater(() -> resultsList.setVisible(false));

                } else {
                    showErrorMessage("Error: No response from the " + type + " details API.");
                }
            } catch (Exception e) {
                showErrorMessage("Error fetching " + type + " details: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchArtistDetails(String artistId) {
        fetchDetails("https://audius-discovery-12.cultur3stake.com/v1/users/" + artistId, "Artist");
    }

    private void fetchAlbumDetails(String albumId) {
        fetchDetails("https://audius-discovery-12.cultur3stake.com/v1/albums/" + albumId, "Album");
    }

    private void fetchPlaylistDetails(String playlistId) {
        String apiUrl = "https://audius-discovery-12.cultur3stake.com/v1/playlists/" + playlistId;
        fetchDetails(apiUrl, "Playlist");
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