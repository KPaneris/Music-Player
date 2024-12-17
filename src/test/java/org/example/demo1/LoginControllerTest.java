package org.example.demo1;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest extends ApplicationTest {

    private TextField textFieldUsername;
    private PasswordField textFieldPassword;
    private TextField visiblePasswordField;
    private CheckBox checkBoxShowPassword;
    private Label errorLabel;
    private Button loginButton;
    private LoginController loginController;

    @Override
    public void start(Stage stage) {
        // Initialize the controller
        loginController = new LoginController();

        // Create real UI components
        textFieldUsername = new TextField();
        textFieldPassword = new PasswordField();
        visiblePasswordField = new TextField();
        visiblePasswordField.setVisible(false); // Initially hidden
        checkBoxShowPassword = new CheckBox("Show Password");
        errorLabel = new Label();
        loginButton = new Button("Login");

        // Link UI components to the controller
        loginController.TextField_Username = textFieldUsername;
        loginController.text_pass_Login = textFieldPassword;
        loginController.visible_pass_Login = visiblePasswordField;
        loginController.check_pass_Login = checkBoxShowPassword;
        loginController.error_login = errorLabel;

        // Add UI components to the layout
        VBox vbox = new VBox(
                textFieldUsername, textFieldPassword, visiblePasswordField, checkBoxShowPassword, loginButton, errorLabel
        );

        // Set up the scene and stage
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();

        // Simulate button click by linking the handler
        loginButton.setOnAction(e -> loginController.handleLoginButton());
        checkBoxShowPassword.setOnAction(e -> loginController.handleShowPassword());
    }

    @Test
    public void testLoginWithValidCredentials() throws Exception {
        // Create a spy for LoginController
        LoginController spyController = spy(loginController);

        // Mock the MainApp instance
        MainApp mockMainApp = mock(MainApp.class);

        // Set the mocked MainApp in the LoginController
        spyController.setMainApp(mockMainApp);

        // Mock the isValidCredentials method
        doReturn(true).when(spyController).isValidCredentials("validUsername", "validPassword");

        // Link the LoginController fields to test UI components
        spyController.TextField_Username = textFieldUsername;
        spyController.text_pass_Login = textFieldPassword;
        spyController.error_login = errorLabel;

        // Simulate user input
        textFieldUsername.setText("validUsername");
        textFieldPassword.setText("validPassword");

        // Simulate login button click
        spyController.handleLoginButton();

        // Verify the navigation was triggered
        verify(mockMainApp).showMusicPlayerPage();

        // Assert no error message
        assertEquals("", errorLabel.getText(), "Error label should be empty when login is successful.");
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Mock isValidCredentials to return false
        LoginController spyController = spy(loginController);
        doReturn(false).when(spyController).isValidCredentials("invalidUser", "wrongPassword");

        // Simulate user input
        textFieldUsername.setText("invalidUser");
        textFieldPassword.setText("wrongPassword");

        // Simulate login button click
        clickOn(loginButton);

        // Assert error message is displayed
        assertEquals("Invalid username or password!", errorLabel.getText(),
                "Error label should show an invalid login message.");
    }

    @Test
    public void testHandleCreateAccountButton() throws Exception {
        // Create a spy for the LoginController
        LoginController spyController = spy(loginController);

        // Mock the MainApp instance
        MainApp mockMainApp = mock(MainApp.class);

        // Set the mocked MainApp in the LoginController
        spyController.setMainApp(mockMainApp);

        // Call the method under test
        spyController.handleCreateAccountButton();

        // Verify that showCreateAccountPage was called once
        verify(mockMainApp, times(1)).showCreateAccountPage();
    }

    @Test
    public void testPasswordVisibility() {
        // Simulate user entering a password and checking the show password checkbox
        textFieldPassword.setText("password123");
        clickOn(checkBoxShowPassword);

        // Assert password field is hidden and visible password field is displayed
        assertFalse(textFieldPassword.isVisible(), "Password field should be hidden when checkbox is selected.");
        assertTrue(visiblePasswordField.isVisible(), "Visible password field should be shown when checkbox is selected.");
        assertEquals("password123", visiblePasswordField.getText(), "Visible password field should display the password.");
    }

    @Test
    public void testPasswordVisibilityWhenUnchecked() {
        // Simulate user entering a password and toggling the checkbox
        textFieldPassword.setText("password123");
        clickOn(checkBoxShowPassword); // Show password
        clickOn(checkBoxShowPassword); // Hide password

        // Assert password field is displayed again
        assertTrue(textFieldPassword.isVisible(), "Password field should be visible when checkbox is unchecked.");
        assertFalse(visiblePasswordField.isVisible(), "Visible password field should be hidden when checkbox is unchecked.");
        assertEquals("password123", textFieldPassword.getText(), "Password field should retain the entered password.");
    }
}
