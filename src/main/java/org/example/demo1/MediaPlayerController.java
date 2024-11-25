package org.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;



import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.scene.image.Image;

public class MediaPlayerController {



    @FXML
    public BorderPane FrameMedia;

    @FXML
    public Button backButton;

    @FXML
    public Button nextButton;
    @FXML

    public ImageView play_pause_icon;
    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseButton, muteButton;

    @FXML
    private Slider volumeSlider, progressSlider;

    @FXML
    private Label currentTimeLabel, totalTimeLabel, songTitleLabel;



    private MediaPlayer mediaPlayer;
    private List<File> playlist = new ArrayList<>();
    private int currentSongIndex = 0;
    private boolean isMuted = false;
    private double previousVolume = 50.0; // Για αποθήκευση της έντασης πριν το Mute

    @FXML
    public void initialize() {
        // Ρύθμιση προεπιλεγμένης έντασης
        volumeSlider.setValue(50);
        mediaPlayer = null; // Αρχικοποίηση του mediaPlayer

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);

                // Αυτόματο Mute αν η ένταση είναι 0
                if (newVal.doubleValue() == 0 && !isMuted) {
                    toggleMute(true); // Ενεργοποίηση Mute
                }

                // Αυτόματο Unmute αν αυξηθεί η ένταση από το 0
                if (newVal.doubleValue() > 0 && isMuted) {
                    toggleMute(false); // Απενεργοποίηση Mute
                }
            }
        });

        // Παύση τραγουδιού όταν πατηθεί το slider
        progressSlider.setOnMousePressed(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            }
        });

        // Μεταφορά στη νέα θέση όταν αφεθεί το slider
        progressSlider.setOnMouseReleased(event -> {
            if (mediaPlayer != null) {
                double progress = progressSlider.getValue() / 100.0;
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progress));
                mediaPlayer.play(); // Συνεχίζει την αναπαραγωγή από το νέο σημείο
            }
        });

        // Όταν κινείται το slider, ενημερώνεται η ένδειξη χρόνου
        progressSlider.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                double progress = progressSlider.getValue() / 100.0;
                double newTimeMillis = mediaPlayer.getTotalDuration().toMillis() * progress;
                currentTimeLabel.setText(formatTime(newTimeMillis));
            }
        });

        // Φόρτωσε τα τραγούδια από τον φάκελο resources/music
        loadSongsFromResources();
    }

    @FXML
    private void loadSongsFromResources() {
        try {
            URL musicFolder = getClass().getResource("mb3");
            if (musicFolder != null) {
                File folder = new File(musicFolder.toURI());
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp3"));

                if (files != null) {
                    for (File file : files) {
                        playlist.add(file);
                    }

                    // Φόρτωσε το πρώτο τραγούδι
                    if (!playlist.isEmpty()) {
                        loadSong(playlist.get(currentSongIndex));
                    }
                } else {
                    System.out.println("Δεν βρέθηκαν τραγούδια στον φάκελο resources/music.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSong(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        String filePath = file.toURI().toString();
        Media media = new Media(filePath);
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration().toMillis()));
            songTitleLabel.setText(file.getName());
            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                updateProgress();
            });

            mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

            playPauseButton.setText("Play");
        });

        mediaPlayer.setOnEndOfMedia(this::playNextSong);
    }



    @FXML
    private void playPauseSong() {
        if (mediaPlayer == null) return;

        // Αλλαγή εικόνας ανάλογα με την κατάσταση του mediaPlayer
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            play_pause_icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("pause.png")))); // Εικόνα για το pause
        } else {
            mediaPlayer.play();
            play_pause_icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("start.png")))); // Εικόνα για το play
        }
    }



    @FXML
    private void playNextSong() {
        if (playlist.isEmpty()) return;

        currentSongIndex = (currentSongIndex + 1) % playlist.size();
        loadSong(playlist.get(currentSongIndex));
        mediaPlayer.play();
    }

    @FXML
    private void playPreviousSong() {
        if (playlist.isEmpty()) return;

        currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        loadSong(playlist.get(currentSongIndex));
        mediaPlayer.play();
    }

    @FXML
    private void toggleMute() {
        if (mediaPlayer == null) return;

        toggleMute(!isMuted); // Εναλλαγή Mute/Unmute
    }

    private void toggleMute(boolean mute) {
        isMuted = mute;
        if (mute) {
            // Αποθήκευση προηγούμενης έντασης και Mute
            previousVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);  // Κλείσιμο της έντασης
            mediaPlayer.setMute(true);
            muteButton.setText("Unmute");
        } else {
            // Επαναφορά προηγούμενης έντασης και Unmute
            volumeSlider.setValue(previousVolume);  // Επαναφορά στην προηγούμενη τιμή έντασης
            mediaPlayer.setMute(false);
            muteButton.setText("Mute");
        }
    }

    private void updateProgress() {
        if (mediaPlayer == null || mediaPlayer.getCurrentTime() == null) return;

        double progress = mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis();
        progressSlider.setValue(progress * 100);

        currentTimeLabel.setText(formatTime(mediaPlayer.getCurrentTime().toMillis()));
    }

    private String formatTime(double millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}
