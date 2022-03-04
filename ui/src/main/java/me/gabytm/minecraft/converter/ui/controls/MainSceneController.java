package me.gabytm.minecraft.converter.ui.controls;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

public class MainSceneController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ListView<String> sourceSelector;
    @FXML
    private ListView<String> targetSelector;

    @FXML
    private Button continueButton;
    @FXML
    private Label label;

    @FXML
    public void initialize() {
        sourceSelector.setItems(FXCollections.observableArrayList("DeluxeMenus", "Test", "Test2"));
        sourceSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           switch (newValue) {
               case "DeluxeMenus" -> targetSelector.setItems(FXCollections.observableArrayList("DM1", "DM2"));
               case "Test" -> targetSelector.setItems(FXCollections.observableArrayList("Test 1", "Test 2"));
               case "Test2" -> targetSelector.setItems(FXCollections.observableArrayList("Test2 1", "Test2 2"));
           }
        });

        continueButton.setOnMouseClicked(event -> {
            if (sourceSelector.getSelectionModel().isEmpty()) {
                label.setText("Select a source plugin!");
                label.setVisible(true);
                return;
            }

            if (targetSelector.getSelectionModel().isEmpty()) {
                label.setText("Select a target plugin!");
                label.setVisible(true);
                return;
            }

            label.setVisible(false);
        });

        label.setVisible(false);
    }

}
