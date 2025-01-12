package org.example.demo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MusicPlayerControllerTest {

    private MusicPlayerController controller;

    @BeforeEach
    void setUp() {
        controller = new MusicPlayerController();
        controller.initialize();
    }

    @Test
    void testSearchBarInitialization() {
        assertNotNull(controller.searchBar, "Search bar should not be null");
        assertEquals("Songs", controller.searchMode.getValue(), "Default search mode should be 'Songs'");
    }

    @Test
    void testEmptySearchResults() {
        controller.searchBar.setText("");
        controller.handleSearch();
        assertTrue(controller.resultsList.getItems().contains("Please enter a search term."),
                "Results should prompt for a search term when empty.");
    }

    @Test
    void testApiUrlBuilding() {
        assertTrue(controller.buildApiUrl("test", "Songs").contains("tracks/search?query=test"),
                "Incorrect URL for songs search.");
        assertTrue(controller.buildApiUrl("artist", "Artists").contains("users/search?query=artist"),
                "Incorrect URL for artists search.");
    }

    @Test
    void testSearchResultsUpdate() {
        String mockResponse = "[{\"id\":\"123\",\"title\":\"Test Song\",\"user\":{\"name\":\"Artist Name\"}}]";
        controller.updateSearchResultsWithStreamUrl(new org.json.JSONArray(mockResponse), "Songs");
        assertTrue(controller.resultsList.getItems().contains("Test Song by Artist Name"),
                "Results should include 'Test Song by Artist Name' after processing mock data.");
    }

    @Test
    void testPlayStreamUrl() {
        assertDoesNotThrow(() -> controller.playStreamUrl(""),
                "Playing stream URL should not throw exceptions.");
    }
}
