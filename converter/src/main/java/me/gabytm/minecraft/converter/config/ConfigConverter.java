package me.gabytm.minecraft.converter.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.function.Function;

public abstract class ConfigConverter {

    protected <T> void convertList(
            @NotNull final Class<T> clazz,
            @NotNull final CommentedConfigurationNode source, @NotNull final NodePath sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final NodePath targetPath,
            @NotNull final Function<List<T>, List<T>> transformer
    ) throws SerializationException {
        source.node(sourcePath).act(node -> {
            if (!node.empty() && node.isList()) {
                target.node(targetPath).setList(clazz, transformer.apply(node.getList(clazz)));
            }
        });
    }

    protected <T> void convertList(
            @NotNull final Class<T> clazz,
            @NotNull final CommentedConfigurationNode source, @NotNull final NodePath sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final NodePath targetPath
    ) throws SerializationException {
        convertList(clazz, source, sourcePath, target, targetPath, Function.identity());
    }

    protected void convertString(
            @NotNull final CommentedConfigurationNode source, @NotNull final NodePath sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final NodePath targetPath,
            @NotNull final Function<String, String> transformer
    ) throws SerializationException {
        source.node(sourcePath).act(node -> {
            if (!node.empty()) {
                final var string = node.getString();

                if (string != null) {
                    target.node(targetPath).set(String.class, transformer.apply(string));
                }
            }
        });
    }

    protected void convertString(
            @NotNull final CommentedConfigurationNode source, @NotNull final NodePath sourcePath,
            @NotNull final CommentedConfigurationNode target, @NotNull final NodePath targetPath
    ) throws SerializationException {
        convertString(source, sourcePath, target, targetPath, Function.identity());
    }

    public abstract void convert(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException;

}
