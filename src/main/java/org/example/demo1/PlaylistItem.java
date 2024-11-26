package org.example.demo1;

// Playlist item model to encapsulate track details
public class PlaylistItem {
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String url;
    private String thumbnail;

    public PlaylistItem(String title, String artist, String album, String duration, String url, String thumbnail) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n" +
                "Artist: " + artist + "\n" +
                "Album: " + album + "\n" +
                "Duration: " + duration + "\n" +
                "URL: " + url + "\n" +
                "Thumbnail: " + thumbnail + "\n";
    }
}

