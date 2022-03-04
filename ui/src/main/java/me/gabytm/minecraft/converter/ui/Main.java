package me.gabytm.minecraft.converter.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final var scene = new Scene(new FXMLLoader(getClass().getClassLoader().getResource("scenes/main/scene.fxml")).load());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("scenes/main/style.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("assets/icon.jpg")));
        primaryStage.setTitle("Converter 2.0.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
