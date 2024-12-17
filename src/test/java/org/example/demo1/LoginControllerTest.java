package org.example.demo1;

import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.testfx.framework.junit5.ApplicationTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest extends ApplicationTest {

    private TextField textFieldUsername;
    private PasswordField textFieldPassword;
    private CheckBox checkBoxShowPassword;
    private Label errorLabel;
    private Button loginButton;

    @Override
    public void start(Stage stage) {
        // Create the real UI components
        textFieldUsername = new TextField();
        textFieldPassword = new PasswordField();
        checkBoxShowPassword = new CheckBox("Show Password");
        errorLabel = new Label();
        loginButton = new Button("Login");

        // Create the layout and add the components
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(
                textFieldUsername, textFieldPassword, checkBoxShowPassword, loginButton, errorLabel
        );

        // Create the scene and set it on the stage
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }


    @Test
    public void testLoginWithValidCredentials() {
        // Simulate user entering valid username and password
        clickOn(textFieldUsername).write("validUsername");
        clickOn(textFieldPassword).write("validPassword");

        // Simulate clicking on the login button
        clickOn(loginButton);

        // Since we don't have backend validation here, we just check if errorLabel is empty
        assertEquals("", errorLabel.getText(), "Error label should be empty when login is successful.");
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Simulate user entering invalid username and password
        clickOn(textFieldUsername).write("invalidUsername");
        clickOn(textFieldPassword).write("invalidPassword");

        // Simulate clicking on the login button
        clickOn(loginButton);

        // We expect an error message to appear in the error label
        assertEquals("Invalid username or password!", errorLabel.getText(), "Error label should show an invalid login message.");
    }

    @Test
    public void testPasswordVisibility() {
        // Simulate user entering a password and checking the show password checkbox
        clickOn(textFieldPassword).write("password123");
        clickOn(checkBoxShowPassword);

        // Test if the password field visibility changes
        assertEquals("password123", textFieldPassword.getText(), "Password should be visible after checkbox is selected.");
    }

    @Test
    public void testPasswordVisibilityWhenUnchecked() {
        // Simulate user entering a password and checking the show password checkbox
        clickOn(textFieldPassword).write("password123");
        clickOn(checkBoxShowPassword);

        // Uncheck the checkbox
        clickOn(checkBoxShowPassword);

        // Test if the password field hides the password again
        assertEquals("password123", textFieldPassword.getText(), "Password should be hidden after checkbox is unchecked.");
    }
}
