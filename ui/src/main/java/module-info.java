module converter.ui {
    requires converter.converter.main;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens me.gabytm.minecraft.converter.ui to javafx.graphics;
    opens me.gabytm.minecraft.converter.ui.controls to javafx.fxml;
}