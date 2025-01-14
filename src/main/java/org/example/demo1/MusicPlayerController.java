package org.example.demo1;


import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.demo1.utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MusicPlayerController {

    @FXML  public Pane center_pane;
    @FXML public Button love_media;
    @FXML public Button playlist_media;
    @FXML private AnchorPane FrameMusicPlayer;
    @FXML public Button back_button;
    @FXML public Label song_name;
    @FXML public Button play_button;
    @FXML public Slider vol_slide;
    @FXML public Button next_button;
    @FXML public Slider slide_song;
    @FXML public Label start_time;
    @FXML public Label end_time;


    private String currentSong;
    private MediaPlayer mediaPlayer;
    private Media media;
    private String currentStreamUrl;

    @FXML TextField searchBar;
    @FXML public ComboBox<String> searchMode;
    @FXML ListView<String> recentSearchesList;
    @FXML ListView<String> resultsList;
    @FXML private Button artist, playlist, mood, settings, love, home, list;
    @FXML private PlaylistItem lastSelectedSongMetadata;
    @FXML private MainApp mainApp;
    @FXML Map<String, ItemInfo> trackMap = new HashMap<>();
    @FXML private final List<String> recentSearches = new LinkedList<>(); // Store recent searches

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        configureTooltips();
        configureSearchBar();
        configureResultsList();
        configureSettingsMenu();

        resultsList.getItems().clear(); // Clear results initially
        resultsList.setVisible(false); // Ensure no results are displayed at startup
        System.out.println("Controller initialized!");
        ObservableList<String> testData = FXCollections.observableArrayList();

        // Wait until the scene is set before accessing its window
        if (FrameMusicPlayer != null && FrameMusicPlayer.getScene() != null) {
            FrameMusicPlayer.getScene().windowProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // Now it's safe to access the window
                    Stage stage = (Stage) newValue;

                    // Debugging: Verify if the setOnCloseRequest is properly attached
                    System.out.println("Setting onCloseRequest listener.");
                    stage.setOnCloseRequest(event -> {
                        System.out.println("Window close event triggered."); // Debugging message
                        saveRecentSearchesToFile(); // Save recent searches when window is closed
                    });
                }
            });
        }

        // Load recent searches from the file
        loadRecentSearchesFromFile();
    }

    private void saveRecentSearchesToFile() {
        // Debugging: Check if save function is being called
        System.out.println("Saving recent searches to file...");

        // Make sure the list is not empty before saving
        if (recentSearches.isEmpty()) {
            System.out.println("No recent searches to save.");
            return; // No recent searches to save
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("recentSearches.txt"))) {
            for (String song : recentSearches) {
                writer.write(song);
                writer.newLine(); // Each song name will be on a new line
            }
            System.out.println("Recent searches saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving recent searches: " + e.getMessage());
        }
    }

    private void loadRecentSearchesFromFile() {
        File file = new File("recentSearches.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Ensure you do not exceed the 10-song limit
                    if (recentSearches.size() < 10) {
                        recentSearches.add(line);
                        recentSearchesList.getItems().add(line);
                    }
                }
                System.out.println("Recent searches loaded from file.");
            } catch (IOException e) {
                System.err.println("Error loading recent searches: " + e.getMessage());
            }
        }
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
        searchMode.setValue("Songs"); // Default search mode
        searchMode.setOnAction(event -> {
            System.out.println("Search mode switched to: " + searchMode.getValue());
            handleSearch(); // Refresh results when search mode changes
        });

        searchBar.setOnMouseClicked(event -> {
            System.out.println("Search bar clicked");
            handleSearch(); // Refresh results when search bar is clicked
        });

        searchBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                System.out.println("Enter pressed");
                handleSearch(); // Refresh results when enter is pressed
            }
        });

        resultsList.setVisible(false); // Ensure results are hidden initially
    }

    private void configureResultsList() {
        resultsList.setVisible(false); // Ensure results list is hidden initially
        resultsList.setOnMouseClicked(this::handleListClick);

        FrameMusicPlayer.setOnMouseClicked(event -> {
            if (!event.getTarget().equals(searchBar) && !event.getTarget().equals(resultsList)) {
                System.out.println("Hiding results list due to outside click.");
                resultsList.setVisible(false); // Hide results list when clicking outside
            }
        });
    }

    // This is the correct method for adding a song to recent searches
    private void addToRecentSearches(String songName, ItemInfo itemInfo) {
        if (recentSearches.size() >= 10) {
            String removedItem = recentSearches.remove(0); // Remove the oldest search if the list exceeds the limit
            recentSearchesList.getItems().remove(removedItem);
        }
        recentSearches.add(songName);
        recentSearchesList.getItems().add(songName);

        // Update the trackMap with the song metadata and track URL
        trackMap.put(songName, itemInfo);

        System.out.println("Added to recent searches: " + songName);
        System.out.println("Recent searches: " + recentSearches);
    }

    private void configureSettingsMenu() {
        ContextMenu settingsMenu = new ContextMenu();
        MenuItem logoutItem = new MenuItem("Log Out");
        logoutItem.setOnAction(event -> handleLogoutAction());
        settingsMenu.getItems().add(logoutItem);

        settings.setOnMouseClicked(event -> {
            if (event.getButton().equals(javafx.scene.input.MouseButton.PRIMARY)) {
                settingsMenu.show(settings, event.getScreenX(), event.getScreenY());
            }
        });
    }

    @FXML
    private void handleLogoutAction() {
        try {
            // Save recent searches before logging out
            saveRecentSearchesToFile();

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

    void updateSearchResultsWithStreamUrl(JSONArray dataArray, String mode) {
        trackMap.clear();
        resultsList.getItems().clear();

        if (dataArray.isEmpty()) {
            resultsList.getItems().add("No results found.");
            System.out.println("Search results are empty.");
            return;
        }

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.optJSONObject(i);
            if (item == null) {
                System.err.println("Skipping null item at index " + i);
                continue;
            }

            String displayText = buildDisplayText(mode, item);
            String itemId = item.optString("id", "");

            if (!itemId.isEmpty()) {
                if ("Songs".equals(mode)) {
                    String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + itemId + "/stream";
                    trackMap.put(displayText, new ItemInfo(itemId, "track", null, streamUrl));
                } else if ("Playlists".equals(mode)) {
                    // Handle playlists separately, adding click event
                    trackMap.put(displayText, new ItemInfo(itemId, "playlist", null, null));
                }
                resultsList.getItems().add(displayText);
            } else {
                System.err.println("Item with missing ID skipped: " + item);
            }
        }
        System.out.println("Search results updated with " + dataArray.length() + " items.");
        resultsList.setVisible(true);
    }

    public String buildApiUrl(String query, String mode) {
        String baseUrl = "https://discoveryprovider2.audius.co/v1/";
        String apiUrl = switch (mode) {
            case "Artists" -> baseUrl + "users/search?query=" + query;
            case "Albums", "Playlists" -> baseUrl + "playlists/search?query=" + query;
            case "Songs" -> baseUrl + "tracks/search?query=" + query;
            default -> null;
        };
        if (apiUrl == null) {
            System.err.println("Unsupported mode: " + mode);
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
        System.out.println("Generated API URL: " + apiUrl);
        return apiUrl;
    }

    @FXML
    void handleSearch() {
        String query = searchBar.getText();
        String selectedMode = searchMode.getValue();

        if (query == null || query.isEmpty()) {
            resultsList.getItems().clear();
            resultsList.getItems().add("Please enter a search term.");
            return;
        }
        resultsList.getItems().clear();
        System.out.println("Initiating search for: " + query + " in mode: " + selectedMode);
        new Thread(() -> {
            try {
                String apiUrl = buildApiUrl(query, selectedMode);
                System.out.println("Fetching data from API: " + apiUrl);

                String response = ApiClient.fetchData(apiUrl);
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray dataArray = jsonResponse.optJSONArray("data");

                if (dataArray != null) {
                    Platform.runLater(() -> updateSearchResultsWithStreamUrl(dataArray, selectedMode));
                } else {
                    System.err.println("No 'data' field in response: " + response);
                    showErrorMessage("No results found. Please refine your search.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Error fetching search results: " + e.getMessage());
            }
        }).start();
    }

    void playStreamUrl(String streamUrl) {
        // Stop the previous playback, if any
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }

        // Create a new Media object with the stream URL
        this.currentStreamUrl = streamUrl;
        Media media = new Media(streamUrl);
        mediaPlayer = new MediaPlayer(media);

        // When the mediaPlayer is ready to play, start playback immediately
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.seek(Duration.ZERO); // Ensure the song starts from the beginning
            mediaPlayer.play(); // Start playback

            // Extract song name from the URL (or metadata if available)
            String songTitle = extractSongNameFromUrl(streamUrl);
            song_name.setText(songTitle); // Display only the song name

            // Set slider maximum value to the song's duration
            slide_song.setMax(media.getDuration().toSeconds());
            vol_slide.setValue(50); // Set initial volume to 50%
            slide_song.setValue(0); // Start the slider at 0

            // Update the song's duration display
            start_time.setText(formatTime(Duration.ZERO));
            end_time.setText(formatTime(media.getDuration()));
        });

        // Bind the slider to the song's current playback time
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            slide_song.setValue(newTime.toSeconds());
            start_time.setText(formatTime(newTime)); // Update start time as the song plays
        });

        // Configure Play/Pause button
        play_button.setText("Pause");
        play_button.setOnAction(event -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                play_button.setText("Play");
            } else {
                mediaPlayer.play();
                play_button.setText("Pause");
            }
        });

        // Configure the volume slider
        vol_slide.valueProperty().addListener((observable, oldValue, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue() / 100.0); // Convert volume to 0.0–1.0 range
        });

        // Allow user to seek through the song using the slider
        slide_song.setOnMousePressed(event -> mediaPlayer.seek(Duration.seconds(slide_song.getValue())));
        slide_song.setOnMouseDragged(event -> mediaPlayer.seek(Duration.seconds(slide_song.getValue())));

        // Reset playback state when the song finishes
        mediaPlayer.setOnEndOfMedia(() -> {
            play_button.setText("Play"); // Reset the Play button text
            slide_song.setValue(0); // Reset the slider
            start_time.setText(formatTime(Duration.ZERO)); // Reset the start time
        });
    }

    //Method to extract song name from URL
    private String extractSongNameFromUrl(String url) {
        // Check if the URL has a query string, and extract the file name accordingly
        String songTitle = url.substring(url.lastIndexOf("/") + 1); // Get the part after the last "/"
        int queryIndex = songTitle.indexOf("?");
        if (queryIndex != -1) {
            songTitle = songTitle.substring(0, queryIndex); // Remove any query parameters
        }
        return songTitle;
    }

    // Μέθοδος για να φορμάρουμε την ώρα
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    void handleListClick(MouseEvent event) {
        // Handle click on a result item (single click)
        if (event.getClickCount() == 1) {
            String selectedItem = resultsList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ItemInfo selectedItemInfo = trackMap.get(selectedItem); // Assume trackMap holds the item details

                if (selectedItemInfo != null) {
                    // Check the type of the selected item and act accordingly
                    switch (selectedItemInfo.getType()) {
                        case "track":  // If it's a song (track)
                            // Add to recent searches (optional)
                            addToRecentSearches(selectedItem, selectedItemInfo);

                            // Update the "Now Playing" label with the song name
                            song_name.setText(selectedItem);  // Show just the song name

                            // Play the stream URL for the selected song
                            playStreamUrl(selectedItemInfo.getTrackUrl());

                            // Hide the result list after selection (optional)
                            resultsList.setVisible(false);
                            break;

                        case "playlist":  // If it's a playlist
                            loadPlaylistDetails(selectedItemInfo.getId());
                            break;

                        case "album":  // If it's an album
                            loadAlbumDetails(selectedItemInfo.getId());
                            break;

                        case "artist":  // If it's an artist
                            loadArtistDetails(selectedItemInfo.getId());
                            break;

                        default:
                            System.out.println("Unknown item type: " + selectedItemInfo.getType());
                            showErrorMessage("Unknown item type: " + selectedItemInfo.getType());
                            break;
                    }
                }
            }
        }
    }

    private void loadArtistDetails(String artistId) {
        new Thread(() -> {
            try {
                // Fetch artist data from the API
                String apiUrl = "https://discoveryprovider2.audius.co/v1/users/" + artistId + "/tracks";
                String response = ApiClient.fetchData(apiUrl);
                JSONObject artistData = new JSONObject(response);

                JSONArray tracks = artistData.optJSONArray("data");

                if (tracks != null) {
                    Platform.runLater(() -> {
                        // Display artist name and tracks in the center_pane
                        displayArtistInCenterPane(artistData.getString("name"), tracks);
                    });
                } else {
                    showErrorMessage("No tracks found for this artist.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Error fetching artist data: " + e.getMessage());
            }
        }).start();
    }

    private void displayArtistInCenterPane(String artistName, JSONArray tracks) {
        // Clear the center_pane before adding new content
        center_pane.getChildren().clear();

        // Display the artist name at the top
        Label artistLabel = new Label("Artist: " + artistName);
        artistLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        center_pane.getChildren().add(artistLabel);

        // Display the track list for the artist
        VBox trackList = new VBox();
        trackList.setSpacing(10);

        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.optJSONObject(i);
            String trackTitle = track.optString("title", "Unknown Track");

            // Create a label for each track
            Label trackLabel = new Label((i + 1) + ". " + trackTitle);
            trackLabel.setStyle("-fx-font-size: 14px;");
            trackLabel.setOnMouseClicked(event -> handleTrackClick(track)); // Set click event for tracks
            trackList.getChildren().add(trackLabel);
        }

        // Add the track list to the center_pane
        center_pane.getChildren().add(trackList);
    }

    private void handleTrackClick(JSONObject track) {
        // This method will handle clicks on individual tracks in the artist view
        String trackTitle = track.optString("title", "Unknown Track");
        String trackUrl = track.optString("url", ""); // Assuming each track has a URL

        if (!trackUrl.isEmpty()) {
            song_name.setText(trackTitle);  // Show song name in the "Now Playing" label
            playStreamUrl(trackUrl);        // Play the track URL
        } else {
            showErrorMessage("Track URL not available for: " + trackTitle);
        }
    }

    private void loadAlbumDetails(String albumId) {
        new Thread(() -> {
            try {
                // Fetch album data from the API
                String apiUrl = "https://discoveryprovider2.audius.co/v1/albums/" + albumId;
                String response = ApiClient.fetchData(apiUrl);
                JSONObject albumData = new JSONObject(response);

                JSONArray tracks = albumData.optJSONArray("tracks");

                if (tracks != null) {
                    Platform.runLater(() -> {
                        // Display album name and tracks in the center_pane
                        displayAlbumInCenterPane(albumData, tracks);
                    });
                } else {
                    showErrorMessage("No tracks found for this album.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Error fetching album data: " + e.getMessage());
            }
        }).start();
        
    }

    private void displayAlbumInCenterPane(JSONObject albumData, JSONArray tracks) {

        // Clear the center_pane before adding new content
        center_pane.getChildren().clear();

        // Set the size of the center_pane
        center_pane.setPrefWidth(1254);
        center_pane.setPrefHeight(513);

        // Create a container for all the content
        VBox entireContent = new VBox(10); // 10px spacing between each part of the content
        entireContent.setStyle("-fx-alignment: top-center; -fx-padding: 20px;");

        // Album header with title, album cover, and artist name
        VBox albumHeader = new VBox(10); // 10px spacing between title, album cover, and stats
        albumHeader.setStyle("-fx-alignment: center; -fx-padding: 20px;");

        // Album title with styling
        String albumName = albumData.optString("album_name", "Unknown Album");
        Label albumLabel = new Label("Album: " + albumName);
        albumLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        albumHeader.getChildren().add(albumLabel);

        // Get the artist name from the album data
        String artistName = albumData.optString("user_name", "Unknown Artist");
        Label artistLabel = new Label("Artist: " + artistName);
        artistLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
        albumHeader.getChildren().add(artistLabel);

        // Get the album cover (if available)
        String albumCoverUrl = albumData.optString("cover_art", "");
        if (!albumCoverUrl.isEmpty()) {
            ImageView albumCoverImage = new ImageView(new Image(albumCoverUrl));
            albumCoverImage.setFitWidth(250); // Set fixed width for consistency
            albumCoverImage.setFitHeight(250); // Set fixed height
            albumCoverImage.setPreserveRatio(true); // Preserve aspect ratio
            albumHeader.getChildren().add(albumCoverImage);
        }

        // Add the album header to the entire content container
        entireContent.getChildren().add(albumHeader);

        // Create a container for the track list
        VBox trackList = new VBox(10); // 10px spacing between tracks
        trackList.setStyle("-fx-padding: 10px; -fx-alignment: top-left;");

        // Add each track's details
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.optJSONObject(i);
            if (track == null) continue;

            String trackTitle = track.optString("title", "Unknown Track");
            String trackArtist = track.optString("user_name", "Unknown Artist");
            String trackLength = formatDuration(track.optInt("duration", 0)); // Assuming 'duration' is in seconds
            int playCount = track.optInt("plays", 0);
            int repostCount = track.optInt("reposts", 0);
            String addedDate = track.optString("added_at", "Unknown Date"); // Assuming the added date is available

            // Create a container for each track with a clean layout (Horizontal arrangement)
            HBox trackBox = new HBox(10); // 10px spacing between elements in the track box
            trackBox.setStyle("-fx-alignment: center-left; -fx-padding: 5px; -fx-background-color: #ECF0F1; -fx-border-radius: 5px; -fx-border-width: 1px; -fx-border-color: #BDC3C7;");

            // Track number label
            Label trackNumberLabel = new Label((i + 1) + ". ");
            trackNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

            // Track name and artist
            Label trackNameLabel = new Label(trackTitle + " by " + trackArtist);
            trackNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

            // Track length, plays, reposts, and added date labels
            Label lengthLabel = new Label("Length: " + trackLength);
            Label playsLabel = new Label("Plays: " + playCount);
            Label repostsLabel = new Label("Reposts: " + repostCount);
            Label addedLabel = new Label("Added: " + addedDate);

            // Add all the track info into the trackBox
            trackBox.getChildren().addAll(trackNumberLabel, trackNameLabel, lengthLabel, playsLabel, repostsLabel, addedLabel);

            // Add the track box to the track list
            trackList.getChildren().add(trackBox);

            // Add mouse click handler for tracks (optional)
            trackBox.setOnMouseClicked(event -> handleTrackClick(track));
        }

        // Add the track list to the entire content container
        entireContent.getChildren().add(trackList);

        // Wrap the entire content container (header + track list) in a ScrollPane to allow scrolling
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(entireContent);  // Set the entire content as the scrollable content
        scrollPane.setFitToWidth(true); // Make scroll pane width match the center_pane width
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Set a fixed height for the scrollPane to enable scrolling
        scrollPane.setPrefHeight(513); // Match the height of the center_pane or adjust as needed

        // Create a StackPane to center the ScrollPane inside the center_pane
        StackPane centeredPane = new StackPane();
        centeredPane.setAlignment(Pos.CENTER); // Center the content both horizontally and vertically
        centeredPane.getChildren().add(scrollPane);  // Add the ScrollPane to the StackPane

        // Add the StackPane (which centers the ScrollPane) to the center_pane
        center_pane.getChildren().add(centeredPane);
    }






    private void loadPlaylistDetails(String playlistId) {
        new Thread(() -> {
            try {
                String apiUrl = "https://discoveryprovider2.audius.co/v1/playlists/" + playlistId + "/tracks";
                String response = ApiClient.fetchData(apiUrl);
                JSONObject playlistData = new JSONObject(response);

                String playlistTitle = playlistData.optString("playlist_name", "Unknown Playlist");
                String albumCoverUrl = playlistData.optString("cover_art", "");

                JSONArray tracks = playlistData.optJSONArray("data");

                if (tracks != null) {
                    Platform.runLater(() -> displayPlaylistInCenterPane(playlistTitle, albumCoverUrl, tracks));
                } else {
                    Platform.runLater(() -> showPopupMessage("No tracks found in this playlist."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showPopupMessage("Error fetching playlist data: " + e.getMessage()));
            }
        }).start();
    }

    private void displayPlaylistInCenterPane(String playlistTitle, String albumCoverUrl, JSONArray tracks) {
        center_pane.getChildren().clear();
        center_pane.setPrefWidth(1254);
        center_pane.setPrefHeight(513);

        VBox entireContent = new VBox(10);
        entireContent.setStyle("-fx-alignment: top-center; -fx-padding: 20px;");

        VBox playlistHeader = new VBox(10);
        playlistHeader.setStyle("-fx-alignment: center; -fx-padding: 20px;");

        Label playlistLabel = new Label(playlistTitle);
        playlistLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        playlistHeader.getChildren().add(playlistLabel);

        if (!albumCoverUrl.isEmpty()) {
            ImageView albumCoverImage = new ImageView(new Image(albumCoverUrl));
            albumCoverImage.setFitWidth(250);
            albumCoverImage.setFitHeight(250);
            albumCoverImage.setPreserveRatio(true);
            playlistHeader.getChildren().add(albumCoverImage);
        }

        entireContent.getChildren().add(playlistHeader);

        VBox trackList = new VBox(10);
        trackList.setStyle("-fx-padding: 10px; -fx-alignment: top-left;");

        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.optJSONObject(i);
            if (track == null) continue;

            String trackTitle = track.optString("title", "Unknown Track");
            String trackArtist = track.optString("user_name", "Unknown Artist");

            HBox trackBox = new HBox(10);
            trackBox.setStyle("-fx-alignment: center-left; -fx-padding: 5px; -fx-background-color: #ECF0F1; -fx-border-radius: 5px; -fx-border-width: 1px; -fx-border-color: #BDC3C7;");

            Label trackNumberLabel = new Label((i + 1) + ". ");
            trackNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

            Label trackNameLabel = new Label(trackTitle + " by " + trackArtist);
            trackNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

            Button addToPlaylistButton = new Button("Add to Playlist");
            addToPlaylistButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
            addToPlaylistButton.setOnAction(event -> showPopupMessage("Added \"" + trackTitle + "\" to Playlist"));

            Button addToLikedSongsButton = new Button("Add to Liked Songs");
            addToLikedSongsButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold;");
            addToLikedSongsButton.setOnAction(event -> showPopupMessage("Added \"" + trackTitle + "\" to Liked Songs"));

            trackBox.getChildren().addAll(trackNumberLabel, trackNameLabel, addToPlaylistButton, addToLikedSongsButton);
            trackList.getChildren().add(trackBox);
        }

        entireContent.getChildren().add(trackList);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(entireContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(513);

        StackPane centeredPane = new StackPane();
        centeredPane.setAlignment(Pos.CENTER);
        centeredPane.getChildren().add(scrollPane);

        center_pane.getChildren().add(centeredPane);
    }

    private void showPopupMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to format duration in seconds to a "mm:ss" format
    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }



    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Something went wrong");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private HttpURLConnection createConnection(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    @FXML
    void handleNextButtonAction() {
        if (!resultsList.getItems().isEmpty()) {
            // Πάρε την τρέχουσα επιλεγμένη θέση στη λίστα
            int currentIndex = resultsList.getSelectionModel().getSelectedIndex();

            // Αν υπάρχει επόμενο στοιχείο, παίξε το
            if (currentIndex + 1 < resultsList.getItems().size()) {
                String nextItem = resultsList.getItems().get(currentIndex + 1);
                resultsList.getSelectionModel().select(currentIndex + 1);
                ItemInfo nextItemInfo = trackMap.get(nextItem);
                if (nextItemInfo != null && "track".equals(nextItemInfo.getType())) {
                    playStreamUrl(nextItemInfo.getTrackUrl()); // Παίξε το επόμενο τραγούδι
                }
            }
        }
    }


    @FXML
    void handleBackButtonAction() {
        if (!resultsList.getItems().isEmpty()) {
            // Πάρε την τρέχουσα επιλεγμένη θέση στη λίστα
            int currentIndex = resultsList.getSelectionModel().getSelectedIndex();

            // Αν υπάρχει προηγούμενο στοιχείο, παίξε το
            if (currentIndex - 1 >= 0) {
                String previousItem = resultsList.getItems().get(currentIndex - 1);
                resultsList.getSelectionModel().select(currentIndex - 1);
                ItemInfo previousItemInfo = trackMap.get(previousItem);
                if (previousItemInfo != null && "track".equals(previousItemInfo.getType())) {
                    playStreamUrl(previousItemInfo.getTrackUrl()); // Παίξε το προηγούμενο τραγούδι
                }
            }
        }
    }




    //ETHO THA KANETE NA EMFANIZONTE OI ARTISt APO TO DATABASE
    public void show_artist(ActionEvent actionEvent) {
        // Create a VBox to hold the title and an empty list view for artists
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));

        // Set the background color of the VBox to match the application theme
        content.setBackground(new Background(new BackgroundFill(Color.web("#1F5F5B"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Add a title label with white text
        Label titleLabel = new Label("My Artists");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white;");

        // Create an empty ListView to display a placeholder for artists
        ListView<String> artistsListView = new ListView<>();
        artistsListView.setPlaceholder(new Label("No artists available"));
        artistsListView.setPrefHeight(200); // Set preferred height for the list view

        // Add the title and list view to the VBox
        content.getChildren().addAll(titleLabel, artistsListView);

        // Stretch the VBox to fill the center pane
        content.setPrefSize(center_pane.getWidth(), center_pane.getHeight());

        // Clear the center pane and add the new content
        center_pane.getChildren().clear();
        center_pane.getChildren().add(content);

        // Apply a fade-in transition for a smooth appearance
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }


    //ETHO THA KANETE NA EMFANIZONTE TA PLAYLIST APO TO DATABASE
    public void show_playlists(ActionEvent actionEvent) {
        // Create a VBox to hold the title and an empty list view for playlists
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));

        // Set the background color of the VBox to match the application theme
        content.setBackground(new Background(new BackgroundFill(Color.web("#1F5F5B"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Add a title label with white text
        Label titleLabel = new Label("My Playlists");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white;");

        // Create an empty ListView to display a placeholder for playlists
        ListView<String> playlistsListView = new ListView<>();
        playlistsListView.setPlaceholder(new Label("No playlists available"));
        playlistsListView.setPrefHeight(200); // Set preferred height for the list view

        // Add the title and list view to the VBox
        content.getChildren().addAll(titleLabel, playlistsListView);

        // Stretch the VBox to fill the center pane
        content.setPrefSize(center_pane.getWidth(), center_pane.getHeight());

        // Clear the center pane and add the new content
        center_pane.getChildren().clear();
        center_pane.getChildren().add(content);

        // Apply a fade-in transition for a smooth appearance
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }


    //ETHO THA KANETE NA EMFANIZONTE TA LIKED SONGS APO TO DATABASE
    public void show_love_songs(ActionEvent actionEvent) {
        // Create a VBox to hold the title and an empty list view
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));

        // Set the background color of the VBox to match the application theme
        content.setBackground(new Background(new BackgroundFill(Color.web("#1F5F5B"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Add a title label with white text
        Label titleLabel = new Label("My Like Songs");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white;");

        // Create an empty ListView to display a placeholder for liked songs
        ListView<String> songsListView = new ListView<>();
        songsListView.setPlaceholder(new Label("No liked songs yet"));
        songsListView.setPrefHeight(200); // Set preferred height for the list view

        // Add the title and list view to the VBox
        content.getChildren().addAll(titleLabel, songsListView);

        // Stretch the VBox to fill the center pane
        content.setPrefSize(center_pane.getWidth(), center_pane.getHeight());

        // Clear the center pane and add the new content
        center_pane.getChildren().clear();
        center_pane.getChildren().add(content);

        // Apply a fade-in transition for a smooth appearance
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }



    //ETHO THA KANEIS NA PROSTHETETE TO TRAGOUTHI APO TO MEDIA STA LIKED SONGS me database
    public void Add_to_love(ActionEvent actionEvent) {
    }

    //ETHO NA PROSTHETETE APO TO MEDIA STO MY  PLAYLIS  me database
    public void ADD_to_playlist(ActionEvent actionEvent) {
    }


    public static class ItemInfo {
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
                        // Log or display the detailed information
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

    private void fetchArtistDetails(String artistId) {
        String apiUrl = "https://discoveryprovider2.audius.co/v1/users/" + artistId;
        fetchDetails(apiUrl, "Artist");
    }

    private void fetchAlbumDetails(String albumId) {
        String apiUrl = "https://discoveryprovider2.audius.co/v1/albums/" + albumId;
        fetchDetails(apiUrl, "Album");
    }

    private void fetchPlaylistDetails(String playlistId) {
        String apiUrl = "https://discoveryprovider2.audius.co/v1/playlists/" + playlistId;
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