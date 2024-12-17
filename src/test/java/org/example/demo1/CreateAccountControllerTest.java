package org.example.demo1;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.testfx.util.WaitForAsyncUtils;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.testfx.framework.junit5.ApplicationTest;

public class CreateAccountControllerTest extends ApplicationTest {

    private CreateAccountController createAccountController;
    @Mock private MainApp mockMainApp;
    @Mock private DatabaseConnection mockDatabaseConnection;

    private TextField text_Create_Account;
    private PasswordField text_pass1;
    private PasswordField text_pass2;
    private TextField visible_pass1;
    private TextField visible_pass2;
    private Label error_create_account;
    private Button create_account_Button_Page;
    private CheckBox check_pass1_Create_Account;

    @Override
    public void start(Stage stage) {
        // Initialize FXML components
        AnchorPane root = new AnchorPane();

        text_Create_Account = new TextField();
        text_pass1 = new PasswordField();
        text_pass2 = new PasswordField();
        visible_pass1 = new TextField();
        visible_pass2 = new TextField();
        error_create_account = new Label();
        create_account_Button_Page = new Button("Create Account");
        check_pass1_Create_Account = new CheckBox("Show Password");

        // Add components to the layout
        root.getChildren().addAll(
                text_Create_Account, text_pass1, text_pass2,
                visible_pass1, visible_pass2, error_create_account,
                create_account_Button_Page, check_pass1_Create_Account
        );

        // Initialize scene and stage
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize controller and inject dependencies
        createAccountController = spy(new CreateAccountController());
        createAccountController.text_Create_Account = text_Create_Account;
        createAccountController.text_pass1 = text_pass1;
        createAccountController.text_pass2 = text_pass2;
        createAccountController.visible_pass1 = visible_pass1;
        createAccountController.visible_pass2 = visible_pass2;
        createAccountController.error_create_account = error_create_account;
        createAccountController.check_pass1_Create_Account = check_pass1_Create_Account;
        createAccountController.setMainApp(mockMainApp);
    }

    @Test
    public void testHandleCreateAccountButton_AllFieldsEmpty() {
        Platform.runLater(() -> {
            createAccountController.text_Create_Account.setText("");
            createAccountController.text_pass1.setText("");
            createAccountController.text_pass2.setText("");
            createAccountController.handleCreateAccountButton();

            assertEquals("All fields are required!", createAccountController.error_create_account.getText());
        });
    }

    @Test
    public void testHandleCreateAccountButton_PasswordsDoNotMatch() {
        // Simulate mismatched passwords
        Platform.runLater(() -> {
            // Simulate mismatched passwords
            createAccountController.text_Create_Account.setText("testUser");
            createAccountController.text_pass1.setText("password1");
            createAccountController.text_pass2.setText("password2");

            //call method
            createAccountController.handleCreateAccountButton();
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Passwords do not match!", createAccountController.error_create_account.getText());
    }

    @Test
    public void testHandleCreateAccountButton_UsernameAlreadyExists() throws Exception {
        // Mock the `isUsernameTaken` method
        CreateAccountController spyController = spy(createAccountController);
        doReturn(true).when(spyController).isUsernameTaken("existingUser");

        // Simulate user input
        Platform.runLater(() -> {
            spyController.text_Create_Account.setText("existingUser");
            spyController.text_pass1.setText("password");
            spyController.text_pass2.setText("password");
            spyController.handleCreateAccountButton();

            // Assert that the correct error message is displayed
            assertEquals("Username already exists!", spyController.error_create_account.getText());
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testHandleCreateAccountButton_SuccessfulAccountCreation() {
        String uniqueUsername = "newUsername_" + System.currentTimeMillis();

        // Mock the method to simulate username availability
        CreateAccountController spyController = spy(createAccountController);
        doReturn(false).when(spyController).isUsernameTaken(uniqueUsername);

        // Simulate user input
        Platform.runLater(() -> {
            spyController.text_Create_Account.setText(uniqueUsername);
            spyController.text_pass1.setText("password");
            spyController.text_pass2.setText("password");

            // Call the method to create the account
            spyController.handleCreateAccountButton();

            // Verify that there is no error message
            assertEquals("", spyController.error_create_account.getText());

            // Optionally verify that the success page is shown (or the relevant method is called)
            try {
                verify(mockMainApp).showLoginPage();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for async events to complete
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testHandleCancelButton_NavigationToLoginPage() throws Exception {
        // Call method
        createAccountController.handleCancelButton();

        // Verify navigation to login page
        verify(mockMainApp, times(1)).showLoginPage();
    }

    @Test
    public void testHandleShowPassword_ToggleVisibility() {
        // Set initial password
        text_pass1.setText("password123");
        text_pass2.setText("password123");

        // Simulate checkbox selection
        check_pass1_Create_Account.setSelected(true);
        createAccountController.handleShowPassword();

        // Verify password fields are visible
        assertTrue(visible_pass1.isVisible());
        assertFalse(text_pass1.isVisible());
        assertEquals("password123", visible_pass1.getText());

        // Simulate checkbox deselection
        check_pass1_Create_Account.setSelected(false);
        createAccountController.handleShowPassword();

        // Verify password fields are hidden
        assertTrue(text_pass1.isVisible());
        assertFalse(visible_pass1.isVisible());
        assertEquals("password123", text_pass1.getText());
    }
}