package me.gabytm.minecraft.converter.ui.scene;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneController {

    private static final Map<SceneController.Scene, javafx.scene.Scene> scenes = new HashMap<>();
    private static Stage stage;

    private static javafx.scene.Scene createScene(final String name) throws IOException {
        final var source = SceneController.class.getClassLoader().getResource("scenes/" + name + "/scene.fxml");
        return new javafx.scene.Scene(new FXMLLoader(source).load());
    }

    public static void load(final Stage stage) throws IOException {
        SceneController.stage = stage;

        scenes.put(Scene.MAIN, createScene("main"));
        scenes.put(Scene.CONFIGS, createScene("configs"));
    }

    public static void switchTo(final Scene scene) {
        stage.setScene(scenes.get(scene));
    }

    public enum Scene {

        MAIN,

        CONFIGS

    }

}
