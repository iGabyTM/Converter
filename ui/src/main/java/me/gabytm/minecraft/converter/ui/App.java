package me.gabytm.minecraft.converter.ui;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.gabytm.minecraft.converter.ui.scene.SceneController;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneController.load(primaryStage);

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("assets/icon.jpg")));
        primaryStage.setTitle("Converter 2.0.0");
        SceneController.switchTo(SceneController.Scene.MAIN);
        primaryStage.show();
    }

}
