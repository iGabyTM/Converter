package me.gabytm.minecraft.converter.config;

import org.spongepowered.configurate.CommentedConfigurationNode;

public abstract class ConfigConverter {

    public abstract void convert(final CommentedConfigurationNode input, final CommentedConfigurationNode output);

}
