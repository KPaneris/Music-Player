package org.example.demo1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL σύνδεσης. Αντικατάστησε το "localhost" και "music_app" με τα στοιχεία σου.
    private static final String URL = "jdbc:mysql://localhost:3305/music_player";
    private static final String USER = "root"; // Αντικατάστησε το με το username της MySQL
    private static final String PASSWORD = "12345"; // Αντικατάστησε το με τον κωδικό σου

    // Δημιουργία σύνδεσης
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
