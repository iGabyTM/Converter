module converter.ui {
    requires javafx.controls;
    requires javafx.fxml;

    opens me.gabytm.minecraft.converter.ui to javafx.graphics;
    opens me.gabytm.minecraft.converter.ui.controls to javafx.fxml;
}