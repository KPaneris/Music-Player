package org.example.demo1;

import org.example.demo1.MusicPlayerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class MusicPlayerControllerTest {

    private MusicPlayerController controller;

    @BeforeEach
    void setUp() {
        // This would typically be part of a JavaFX test environment.
        // The controller needs to be instantiated and UI components should be set up (mocked or real).
        controller = new MusicPlayerController();
        controller.initialize(); // Initialize the controller to set up default UI state.
    }

    @Test
    void testSearchBarInitialization() {
        // Check if the search bar is initialized correctly
        assertNotNull(controller.searchBar, "Search bar should not be null");
        assertEquals("Songs", controller.searchMode.getValue(), "Search mode should default to 'Songs'");
    }

    @Test
    void testEmptySearchResults() {
        // Simulate an empty search (no query entered)
        controller.searchBar.setText("");
        controller.handleSearch();

        // Check if the results list shows the message "Please enter a search term."
        assertTrue(controller.resultsList.getItems().contains("Please enter a search term."),
                "The results list should contain 'Please enter a search term.' when the search is empty.");
    }

    @Test
    void testApiUrlBuilding() {
        // Test URL building for 'Songs'
        String url = controller.buildApiUrl("test query", "Songs");
        assertTrue(url.contains("tracks/search?query=test query"), "API URL for songs is incorrect.");

        // Test URL building for 'Artists'
        url = controller.buildApiUrl("artist query", "Artists");
        assertTrue(url.contains("users/search?query=artist query"), "API URL for artists is incorrect.");
    }

    @Test
    void testSearchResultsUpdate() {
        // Mock a response and simulate updating the search results.
        String mockResponse = "{ \"data\": [ { \"id\": \"123\", \"title\": \"Test Song\", \"user\": {\"name\": \"Artist Name\"} } ] }";

        // Simulate the response handling logic
        controller.updateSearchResultsWithStreamUrl(new org.json.JSONArray(mockResponse), "Songs");

        // Ensure results were updated with track names.
        assertTrue(controller.resultsList.getItems().contains("Test Song by Artist Name"),
                "Results list should contain 'Test Song by Artist Name' after processing the mock response.");
    }

    @Test
    void testPlayStreamUrl() {
        // Assuming we mock the MediaPlayer class or test with a valid URL in an actual environment.
        // Here we just check if the playStreamUrl method runs without exceptions.
        try {
            controller.playStreamUrl("");
        } catch (Exception e) {
            fail("Error playing stream URL: " + e.getMessage());
        }
    }
}
