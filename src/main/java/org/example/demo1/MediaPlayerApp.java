package org.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MediaPlayerApp extends Application {

    private Object controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("media.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        controller = fxmlLoader.getController(); // Store the controller reference
        stage.setTitle("Media Player");
        stage.setScene(scene);
        stage.show();
    }

    public Object getController() {
        return controller;
    }
    public static void main(String[] args) {
        launch(args);
    }
}
