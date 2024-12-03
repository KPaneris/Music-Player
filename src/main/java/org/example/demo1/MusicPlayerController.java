package org.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javax.swing.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.example.demo1.utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicPlayerController {
    @FXML private Button artist, playlist, mood, settings, love, home, list;
    @FXML private AnchorPane FrameMusicPlayer;
    @FXML private TextField searchBar; // Input field for search text.
    @FXML private ComboBox<String> searchMode; // Dropdown for search mode
    @FXML private ListView<String> resultsList; // ListView to display search results.
    @FXML private MediaPlayer mediaPlayer; // MediaPlayer to handle audio playback
    @FXML private MediaView mediaView; // MediaView for optional audio visualization
    @FXML private PlaylistItem lastSelectedSongMetadata; // To hold details of the last selected track

    private Map<String, ItemInfo> trackMap = new HashMap<>(); // Map for storing track details

    @FXML
    public void initialize() {
        configureTooltips();
        configureSearchBar();
        configureResultsList();
        configureSettingsMenu();
        fetchTracks(); // Load trending tracks on initialization
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
        tooltip.setShowDelay(javafx.util.Duration.millis(100)); // Faster display
        tooltip.setHideDelay(javafx.util.Duration.millis(100)); // Faster hide
        button.setTooltip(tooltip);
    }

    private void configureSearchBar() {
        searchMode.setValue("Songs");
        searchMode.setOnAction(event -> System.out.println("Search mode switched to: " + searchMode.getValue()));
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                handleSearch(); // Trigger search on Enter
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

                // Update search results with streaming URLs
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
            String trackId = item.optString("id", null);

            if (trackId != null && !trackId.isEmpty()) {
                // Create the stream URL
                String streamUrl = "https://audius-discovery-12.cultur3stake.com/v1/tracks/" + trackId + "/stream";
                trackMap.put(displayText, new ItemInfo(trackId, "track", null, streamUrl));
                resultsList.getItems().add(displayText);
            } else {
                System.err.println("Skipping item due to missing track_id.");
            }
        }
        resultsList.setVisible(true);
    }


    private HttpURLConnection createConnection(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
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

    private String buildApiUrl(String query, String mode) {
        String baseUrl = "https://audius-discovery-12.cultur3stake.com/v1/";
        switch (mode) {
            case "Artists":
                return baseUrl + "users/search?query=" + query;
            case "Albums":
            case "Playlists":
                return baseUrl + "playlists/search?query=" + query;
            case "Songs":
                return baseUrl + "tracks/search?query=" + query;
            default:
                return baseUrl + "tracks/search?query=" + query;
        }
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

    public class ItemInfo {
        private String id;
        private String type;
        private String cid; // Optional field for CID
        private String trackUrl; // Field for streaming URL

        public ItemInfo(String id, String type, String cid, String trackUrl) {
            this.id = id;
            this.type = type;
            this.cid = cid;
            this.trackUrl = trackUrl;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getCid() {
            return cid;
        }

        public String getTrackUrl() {
            return trackUrl;
        }
    }

    @FXML
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            String selectedTrack = resultsList.getSelectionModel().getSelectedItem();
            System.out.println("Selected item: " + selectedTrack); // Debug log

            if (selectedTrack != null) {
                ItemInfo selectedItemInfo = trackMap.get(selectedTrack);
                if (selectedItemInfo != null) {
                    System.out.println("Item found in trackMap with stream URL: " + selectedItemInfo.getTrackUrl());
                    playStreamUrl(selectedItemInfo.getTrackUrl());
                } else {
                    System.out.println("Item not found in trackMap."); // Debug log
                }
            } else {
                System.out.println("No item selected."); // Debug log
            }
        }
    }

    private void playStreamUrl(String streamUrl) {
        Platform.runLater(() -> {
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

    private void handleItemSelection(String selectedItem, ItemInfo itemInfo) {
        if (itemInfo != null) {
            System.out.println("Item selected: " + selectedItem + " of type: " + itemInfo.getType());
            switch (itemInfo.getType()) {
                case "track":
                    String trackId = itemInfo.getId();
                    System.out.println("Fetching track details for ID: " + trackId);
                    fetchTrackDetails(trackId);
                    break;
                case "artist":
                    String artistId = itemInfo.getId();
                    System.out.println("Fetching artist details for ID: " + artistId);
                    fetchArtistDetails(artistId);
                    break;
                case "album":
                    String albumId = itemInfo.getId();
                    System.out.println("Fetching album details for ID: " + albumId);
                    fetchAlbumDetails(albumId);
                    break;
                case "playlist":
                    String playlistId = itemInfo.getId();
                    System.out.println("Fetching playlist details for ID: " + playlistId);
                    fetchPlaylistDetails(playlistId);
                    break;
                default:
                    System.out.println("Unknown item type: " + itemInfo.getType());
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
                } else {
                    showErrorMessage("Error: No response from the " + type + " details API.");
                }
            } catch (Exception e) {
                showErrorMessage("Error fetching " + type + " details: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchTrackDetails(String trackId) {
        new Thread(() -> {
            try {
                String apiUrl = "https://discoveryprovider.audius.co/v1/tracks/" + trackId;
                String response = fetchDataFromApi(apiUrl);

                if (response != null) {
                    JSONObject trackDetails = new JSONObject(response);
                    String trackCid = trackDetails.optString("track_cid", null);

                    if (trackCid != null && !trackCid.isEmpty()) {
                        System.out.println("Track CID retrieved: " + trackCid);
                        // Update trackMap or use trackCid as needed
                    } else {
                        System.err.println("Error: Track CID not found in response.");
                    }
                } else {
                    System.err.println("Error: No response from track details API.");
                }
            } catch (Exception e) {
                System.err.println("Error fetching track details: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchTrackUrl(String permalink) {
        new Thread(() -> {
            try {
                // Use Audius API to resolve the actual media URL if necessary
                String resolvedMediaUrl = resolveMediaUrl(permalink);

                // Debug log
                System.out.println("Resolved media URL: " + resolvedMediaUrl);

                if (resolvedMediaUrl != null) {
                    Platform.runLater(() -> {
                        try {
                            Media media = new Media(resolvedMediaUrl);
                            mediaPlayer = new MediaPlayer(media);
                            mediaPlayer.play();
                        } catch (Exception e) {
                            showErrorMessage("Error playing track: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    showErrorMessage("Error: Could not resolve media URL.");
                }
            } catch (Exception e) {
                showErrorMessage("Error resolving track URL: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private String resolveMediaUrl(String trackId) throws IOException {
        String apiUrl = "https://audius-discovery-12.cultur3stake.com/v1/tracks/" + trackId + "/stream";
        HttpURLConnection connection = createConnection(apiUrl);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = new String(connection.getInputStream().readAllBytes());
            System.out.println("Raw JSON response from stream URL resolution: " + response);

            try {
                JSONObject jsonResponse = new JSONObject(response);
                String streamUrl = jsonResponse.optString("data", null);

                if (streamUrl != null) {
                    return streamUrl;
                } else {
                    System.err.println("Error: 'data' field missing in JSON response: " + response);
                    return null;
                }
            } catch (JSONException e) {
                System.err.println("Invalid JSON response: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("Error fetching stream URL: HTTP " + responseCode);
            return null;
        }
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