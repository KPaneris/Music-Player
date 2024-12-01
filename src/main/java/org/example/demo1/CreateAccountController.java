package org.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import javax.swing.*;

public class CreateAccountController {

    @FXML

    public AnchorPane FrameAccountApplication;
    @FXML

    public Button create_account_Button_Page;
    @FXML

    public CheckBox check_pass1_Create_Account;
    @FXML

    public Button cancel_Button;
    @FXML

    public PasswordField text_pass2;
    @FXML

    public PasswordField text_pass1;


    @FXML

    public TextField text_Create_Account;


    @FXML
    private Label error_create_account;

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

        // Simulate account creation logic (e.g., saving to database)
        System.out.println("Account created for username: " + username);

        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password1); // Ιδανικά, κρυπτογράφησε το password
            stmt.executeUpdate();
            mainApp.showLoginPage();
        } catch (SQLException e) {
            error_create_account.setText("Username already exists!");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    // Μέθοδος για να διαχειριστεί την επιλογή του checkbox
    @FXML
    public void handleShowPassword() {
        if (check_pass1_Create_Account.isSelected()) {
            // Αν το checkbox είναι επιλεγμένο, εμφανίζουμε τον κωδικό ως κανονικό κείμενο

            text_pass1.setPromptText(text_pass1.getText());
            text_pass1.setText("");

            text_pass2.setPromptText(text_pass2.getText());
            text_pass2.setText("");
        } else {
            // Αν δεν είναι επιλεγμένο το checkbox, εμφανίζουμε τον κωδικό ως αστερίσκους

            text_pass1.setText(text_pass1.getPromptText());
            text_pass1.setPromptText("");

            text_pass2.setText(text_pass2.getPromptText());
            text_pass2.setPromptText("");
        }






    }

}
