module converter.converter.main {
    requires java.base;
    requires java.sql;
    requires org.apache.commons.lang3;
    requires org.spongepowered.configurate;
    requires org.spongepowered.configurate.yaml;

    exports me.gabytm.minecraft.converter.config to converter.ui;
}