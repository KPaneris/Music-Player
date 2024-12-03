package org.example.demo1.media;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MediaPlayback {
    private MediaPlayer mediaPlayer;

    public void playStream(String streamUrl) {
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Stop any existing playback
        }

        Platform.runLater(() -> {
            try {
                Media media = new Media(streamUrl);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("Error playing media: " + e.getMessage());
            }
        });
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
