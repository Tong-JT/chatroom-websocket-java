package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        UserInterface GUI = new UserInterface();
        Scene scene = new Scene(GUI, 800, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Chatroom");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}