package org.example.demo1.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {
    public static HttpURLConnection createConnection(String apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    public static String fetchData(String apiUrl) throws IOException {
        HttpURLConnection connection = createConnection(apiUrl);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return new String(connection.getInputStream().readAllBytes());
        } else {
            throw new IOException("Received HTTP " + connection.getResponseCode());
        }
    }
}
