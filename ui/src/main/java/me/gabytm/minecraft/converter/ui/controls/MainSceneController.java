package me.gabytm.minecraft.converter.ui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import me.gabytm.minecraft.converter.ui.scene.SceneController;

public class MainSceneController {

    @FXML
    private Button configsButton;

    @FXML
    private void initialize() {
        configsButton.setOnMouseClicked(event -> SceneController.switchTo(SceneController.Scene.CONFIGS));
    }

}
