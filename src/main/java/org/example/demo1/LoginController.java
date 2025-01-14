package org.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private AnchorPane FrameLogin;
    @FXML public TextField TextField_Username;
    @FXML public PasswordField text_pass_Login;
    @FXML private Button Login_Button;
    @FXML private Button create_account_Login;
    @FXML public CheckBox check_pass_Login;
    @FXML public Label error_login;
    public static int currentUserId ;

    private MainApp mainApp;
    private Map<String, String> users = new HashMap<>();
    public TextField visible_pass_Login;
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void handleLoginButton() {

        String username = TextField_Username.getText();
        String password = check_pass_Login.isSelected() ? visible_pass_Login.getText() : text_pass_Login.getText();


        // Debugging: Print to console
        System.out.println("Entered Username: " + username);
        System.out.println("Entered Password: " + password);

        int userId = isValidCredentials(username, password);
        if (userId != -1) {
            currentUserId = userId;
            mainApp.setCurrentUserId(userId);
            try {
                System.out.println("User logged in successfully with ID: " + userId);
                error_login.setText("Welcome! Your User ID: " + userId);



                mainApp.showMusicPlayerPage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            error_login.setText("Invalid username or password!");
        }
    }

    // Μέθοδος για να διαχειριστεί την επιλογή του checkbox
    @FXML
    public void handleShowPassword() {
        if (check_pass_Login.isSelected()) {
            // Show password
            visible_pass_Login.setText(text_pass_Login.getText());
            visible_pass_Login.setVisible(true);
            text_pass_Login.setVisible(false);
        } else {
            // Hide password
            text_pass_Login.setText(visible_pass_Login.getText());
            text_pass_Login.setVisible(true);
            visible_pass_Login.setVisible(false);
        }
    }

    @FXML
    public void handleCreateAccountButton() {
        try {
            mainApp.showCreateAccountPage(); // Navigate to Create Account Page
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public int isValidCredentials(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1 ;

    }
}