package me.gabytm.minecraft.converter.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public abstract class ConfigConverter {

    public abstract void convert(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException;

}
