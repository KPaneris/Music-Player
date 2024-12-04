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

    public TextField visible_pass_Login;
    @FXML
    private AnchorPane FrameLogin;

    @FXML
    private TextField TextField_Username;

    @FXML
    private PasswordField text_pass_Login;

    @FXML
    private Button Login_Button;

    @FXML
    private Button create_account_Login;

    @FXML
    private CheckBox check_pass_Login;

    @FXML
    private Label error_login;

    private MainApp mainApp;

    private Map<String, String> users = new HashMap<>();
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

        // Validate credentials
        if (isValidCredentials(username, password)) {
            try {
                mainApp.showMusicPlayerPage(); // Navigate to music player
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


    private boolean isValidCredentials(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Επιστρέφει true αν βρεθεί το username και password
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}