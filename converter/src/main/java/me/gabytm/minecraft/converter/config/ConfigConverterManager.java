package me.gabytm.minecraft.converter.config;

import me.gabytm.minecraft.converter.config.implementations.ChestCommandsToDeluxeMenusConfigConverter;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigConverterManager {

    private final Map<String, Map<String, ConfigConverter>> converters = new HashMap<>();
    private final List<String> sources;

    public ConfigConverterManager() {
        converters.put("ChestCommands", Map.of("DeluxeMenus", new ChestCommandsToDeluxeMenusConfigConverter()));
        sources = Arrays.stream(converters.keySet().toArray(String[]::new))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<String> getSources() {
        return this.sources;
    }

    public List<String> getTargets(final String source) {
        return Arrays.stream(this.converters.get(source).keySet().toArray(String[]::new))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public void convert(final File file, final String source, final String target) {
        final var f = new File(target, file.getName());

        if (!f.exists()) {
            f.getParentFile().mkdirs();

            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            final var sourceLoader = YamlConfigurationLoader.builder()
                    .file(file)
                    .build();
            final var targetLoader = YamlConfigurationLoader.builder()
                    .file(f)
                    .indent(2)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            final var sourceRoot = sourceLoader.load();
            final var targetRoot = targetLoader.load();

            converters.get(source).get(target).convert(sourceRoot, targetRoot);
            targetLoader.save(targetRoot);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

}
