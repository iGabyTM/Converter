package me.gabytm.minecraft.converter.config.implementations;

import me.gabytm.minecraft.converter.config.ConfigConverter;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.spongepowered.configurate.NodePath.path;

public class ChestCommandsToDeluxeMenusConfigConverter extends ConfigConverter {

    private static final int SLOTS_PER_ROW = 9;
    private static final Map<String, String> PLACEHOLDERS = Map.of(
            "{player}", "%player_name%",
            "{money}", "%vault_eco_balance%",
            "{world}", "%player_world%",
            "{online}", "%server_online",
            "{max_players}", "%server_max%"
    );

    private String convertPlaceholders(final String input) {
        return StringUtils.replaceEach(input, PLACEHOLDERS.keySet().toArray(String[]::new), PLACEHOLDERS.values().toArray(String[]::new));
    }

    private List<String> convertActions(final List<String> ccActions) {
        final var actions = new ArrayList<String>(ccActions.size());

        for (final var action : ccActions) {
            final var parts = action.split(": ", 2);

            if (parts.length == 1) {
                actions.add("[player] " + action);
                continue;
            }

            final var arguments = convertPlaceholders(parts[1]);

            final var dmAction = switch (parts[0].toLowerCase()) {
                case "broadcast" -> "[broadcast] " + arguments;
                case "console" -> "[console] " + arguments;
                case "give-money" -> "[givemoney] " + arguments;
                case "open" -> "[openguimenu] " + arguments.replace(".yml", "");
                case "server" -> "[connect] " + arguments;
                case "tell" -> "[message] " + arguments;
                default -> null;
            };

            if (dmAction != null) {
                actions.add(dmAction);
            }
        }

        return actions;
    }

    private int convertSlot(final int x, final int y) {
        return SLOTS_PER_ROW * (y - 1) + (x - 1);
    }

    private void convertMenuSettings(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException {
        input.act(settings -> {
            output.node("gui_title").set(String.class, settings.node("name").getString());
            output.node("size").set(Integer.class, settings.node("rows").getInt() * SLOTS_PER_ROW);

            final var commandsNode = settings.node("commands");

            output.node("open_command")
                    .setList(String.class, commandsNode.empty() ? List.of("opencommand") : commandsNode.getList(String.class));

            settings.node("auto-refresh").act(autoRefresh -> {
                if (!autoRefresh.empty()) {
                    output.node("update_interval").set(Integer.class, autoRefresh.getInt());
                }
            });

            settings.node("open-actions").act(openActions -> {
                if (!openActions.empty()) {
                    output.node("open_commands").setList(String.class, convertActions(openActions.getList(String.class)));
                }
            });
            convertList(String.class, settings, path("open-actions"), output, path("open_commands"), this::convertActions);
        });
    }

    private void convertItems(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException {
        convertString(
                input, path("MATERIAL"), output, path("material"),
                material -> material.toUpperCase().replace(" ", "_")
        );
        input.node("DURABILITY").act(durability -> {
            if (!durability.empty()) {
                output.node("data").set(Short.class, (short) durability.getInt());
            }
        });
        output.node("amount").set(Integer.class, Math.max(1, input.node("AMOUNT").getInt()));
        output.node("slot").set(Integer.class, convertSlot(input.node("POSITION-X").getInt(), input.node("POSITION-Y").getInt()));
        convertString(input, path("COLOR"), output, path("rgb"), color -> color.replace(" ", ""));
        convertString(input, path("NAME"), output, path("name"), this::convertPlaceholders);
        convertList(
                String.class, input, path("LORE"), output, path("lore"),
                lore -> lore.stream().map(this::convertPlaceholders).collect(Collectors.toList())
        );
        convertList(
                String.class, input, path("BANNER-PATTERNS"), output, path("banner_meta"),
                list -> list.stream()
                        .map(String::toUpperCase)
                        .map(it -> {
                            final var parts = it.split(":");
                            return (parts.length == 2) ? parts[1] + ';' + parts[0] : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        convertList(String.class, input, path("ACTIONS"), output, path("click_actions"), this::convertActions);
    }

    @Override
    public void convert(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException {
        final var menuSettings = input.node("menu-settings");

        if (menuSettings.virtual() || menuSettings.empty()) {
            return;
        }

        convertMenuSettings(menuSettings, output);

        final var itemsSection = output.node("items");

        for (final var iconSection : input.childrenMap().values()) {
            if (iconSection.key().toString().equals("menu-settings")) {
                continue;
            }

            convertItems(iconSection, itemsSection.node(iconSection.key().toString()));
        }
    }

}
