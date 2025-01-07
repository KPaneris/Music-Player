package org.example.demo1;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.demo1.utils.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MusicPlayerController {


    @FXML
    private AnchorPane FrameMusicPlayer;



//Media player

    @FXML public Button back_button;
    @FXML public Label song_name;
    @FXML public Button pause_button;
    @FXML public Button play_button;
    @FXML public Slider vol_slide;
    @FXML public Button next_button;
    @FXML public Slider slide_song;
    @FXML public Label start_time;
    @FXML  public Label end_time;

    private MediaPlayer mediaPlayer;
    private Media media;
    private String currentStreamUrl;




    @FXML
    public TextField searchbar;
    @FXML
    TextField searchBar;
    @FXML
    public ComboBox<String> searchMode;
    @FXML
    ListView<String> resultsList;




    @FXML
    private Button artist, playlist, mood, settings, love, home, list;




    @FXML
    private PlaylistItem lastSelectedSongMetadata;


    @FXML
    private MainApp mainApp;




    @FXML
    Map<String, ItemInfo> trackMap = new HashMap<>();


    @FXML
    private final List<String> recentSearches = new LinkedList<>(); // Store recent searches


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

        }
        recentSearches.add(songName);


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
                } else {
                    String itemType = switch (mode) {
                        case "Artists" -> "artist";
                        case "Albums" -> "album";
                        case "Playlists" -> "playlist";
                        default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
                    };
                    trackMap.put(displayText, new ItemInfo(itemId, itemType, null, null));
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
        // Σταματάμε την προηγούμενη αναπαραγωγή, αν υπάρχει
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }

        // Δημιουργούμε νέο Media αντικείμενο με το URL του stream
        this.currentStreamUrl = streamUrl;
        Media media = new Media(streamUrl);
        mediaPlayer = new MediaPlayer(media);

        // Όταν το mediaPlayer είναι έτοιμο για αναπαραγωγή, το ξεκινάμε αμέσως
        mediaPlayer.setOnReady(() -> {
            // Εξασφαλίζουμε ότι το τραγούδι ξεκινά από την αρχή
            mediaPlayer.seek(Duration.ZERO);  // Θέτουμε την τρέχουσα θέση στο 0, για να ξεκινήσει το τραγούδι σωστά
            mediaPlayer.play(); // Ξεκινάμε την αναπαραγωγή

            // Ενημερώνουμε το πεδίο "Now Playing" με το όνομα του τραγουδιού
            String songTitle = media.getSource().substring(media.getSource().lastIndexOf("/") + 1);  // Λαμβάνουμε το όνομα του τραγουδιού από το URL
            song_name.setText("Now Playing: " + songTitle);  // Ενημερώνουμε το "Now Playing" μόνο όταν το τραγούδι ξεκινήσει να παίζει

            slide_song.setMax(media.getDuration().toSeconds()); // Ορίζουμε το max του slider ίσο με τη διάρκεια του τραγουδιού
            vol_slide.setValue(50);  // Αρχική ένταση 50%
            slide_song.setValue(0);  // Ξεκινάμε το slider στο 0

            // Ενημέρωση για τη διάρκεια του τραγουδιού
            start_time.setText(formatTime(Duration.ZERO));  // Αρχική ώρα εκκίνησης στο 0
            end_time.setText(formatTime(media.getDuration())); // Ενημερώνουμε την ώρα λήξης
        });

        // Ρύθμιση Play/Pause button
        play_button.setText("Pause");
        play_button.setOnAction(event -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause(); // Παύση του τραγουδιού
                play_button.setText("Play"); // Εμφανίζουμε το κουμπί Play
            } else {
                mediaPlayer.play(); // Συνέχιση του τραγουδιού
                play_button.setText("Pause"); // Εμφανίζουμε το κουμπί Pause
            }
        });

        // Ρύθμιση του volume slider
        vol_slide.valueProperty().addListener((observable, oldValue, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue() / 100.0); // Αλλαγή έντασης από 0-100 σε 0.0-1.0
        });

        // Ενημέρωση της θέσης του τραγουδιού μέσω του slider
        slide_song.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() != oldValue.doubleValue()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue())); // Άμεση αναζήτηση στη νέα θέση
            }
        });

        // Ενημέρωση της κατάσταση του τραγουδιού όταν ολοκληρωθεί
        mediaPlayer.setOnEndOfMedia(() -> {
            play_button.setText("Play"); // Επαναφορά του κουμπιού Play
        });
    }

    // Μέθοδος για να φορμάρουμε την ώρα
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }



    @FXML
    void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 1) {  // Όταν γίνει κλικ σε ένα τραγούδι
            String selectedItem = resultsList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ItemInfo selectedItemInfo = trackMap.get(selectedItem);
                if (selectedItemInfo != null && "track".equals(selectedItemInfo.getType())) {

                    // Ενημέρωση του πεδίου Now Playing
                    song_name.setText("Now Playing: " + selectedItem);  // Το όνομα του τραγουδιού από τη λίστα

                    // Προσθήκη του τραγουδιού στη λίστα των πρόσφατων αναζητήσεων
                    addToRecentSearches(selectedItem, selectedItemInfo);

                    // Παίζουμε το τραγούδι
                    playStreamUrl(selectedItemInfo.getTrackUrl());

                    // Κλείνουμε τη λίστα αποτελεσμάτων
                    resultsList.setVisible(false);
                }
            }
        }
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