package me.gabytm.minecraft.converter.ui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import me.gabytm.minecraft.converter.config.ConfigConverterManager;

import java.io.File;

import static javafx.collections.FXCollections.observableArrayList;

public class ConfigsSceneController {

    @FXML
    private ListView<String> sourceSelector;
    @FXML
    private ListView<String> targetSelector;

    @FXML
    private Button convertButton;
    @FXML
    private Text label;

    @FXML
    public void initialize() {
        final var manager = new ConfigConverterManager();

        sourceSelector.setItems(observableArrayList(manager.getSources()));
        sourceSelector.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> targetSelector.setItems(observableArrayList(manager.getTargets(newValue))));

        convertButton.setOnMouseClicked(event -> {
            final var source = sourceSelector.getSelectionModel().getSelectedItem();

            if (source.isEmpty()) {
                label.setText("Select a source plugin!");
                label.setVisible(true);
                return;
            }

            final var target = targetSelector.getSelectionModel().getSelectedItem();

            if (target.isEmpty()) {
                label.setText("Select a target plugin!");
                label.setVisible(true);
                return;
            }

            label.setVisible(false);
            manager.convert(new File(source + ".yml"), source, target);
        });

        label.setVisible(false);
    }

}
