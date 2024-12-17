package org.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javax.swing.*;

public class CreateAccountController {

    @FXML public AnchorPane FrameAccountApplication;
    @FXML public Button create_account_Button_Page;
    @FXML public CheckBox check_pass1_Create_Account;
    @FXML public Button cancel_Button;
    @FXML public PasswordField text_pass2;
    @FXML public PasswordField text_pass1;
    @FXML public TextField text_Create_Account;
    @FXML private Label error_create_account;

    public TextField visible_pass1;
    public TextField visible_pass2;
    private MainApp mainApp;
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void handleCreateAccountButton() {
        String username = text_Create_Account.getText();
        String password1 = text_pass1.getText();
        String password2 = text_pass2.getText();

        // Validate input fields
        if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            error_create_account.setText("All fields are required!");
            return;
        }

        if (!password1.equals(password2)) {
            error_create_account.setText("Passwords do not match!");
            return;
        }

        if (isUsernameTaken(username)) {
            error_create_account.setText("Username already exists!");
            return;
        }

        // Insert the new user into the database
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password1); // Consider encrypting the password
            stmt.executeUpdate();
            mainApp.showLoginPage(); // Navigate to the login page
        } catch (SQLException e) {
            error_create_account.setText("An error occurred while creating the account.");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a result exists, the username is already taken
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume the username is taken if there's an error
        }
    }

    @FXML
    public void handleCancelButton() {
        // Return to the login page without creating an account
        try {
            mainApp.showLoginPage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Μέθοδος για να διαχειριστεί την επιλογή του checkbox
    @FXML
    public void handleShowPassword() {
        if (check_pass1_Create_Account.isSelected()) {
            // Show passwords
            visible_pass1.setText(text_pass1.getText());
            visible_pass2.setText(text_pass2.getText());
            visible_pass1.setVisible(true);
            text_pass1.setVisible(false);
            visible_pass2.setVisible(true);
            text_pass2.setVisible(false);
        } else {
            // Hide passwords
            text_pass1.setText(visible_pass1.getText());
            text_pass2.setText(visible_pass2.getText());
            text_pass1.setVisible(true);
            visible_pass1.setVisible(false);
            text_pass2.setVisible(true);
            visible_pass2.setVisible(false);
        }
    }
}