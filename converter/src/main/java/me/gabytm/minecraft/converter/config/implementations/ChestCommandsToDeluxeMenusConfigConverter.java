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
        });
    }

    private void convertItems(final CommentedConfigurationNode input, final CommentedConfigurationNode output) throws SerializationException {
        output.node("material").set(String.class, input.node("MATERIAL").getString().toUpperCase().replace(" ", "_"));
        input.node("DURABILITY").act(durability -> {
            if (!durability.empty()) {
                output.node("data").set(Short.class, (short) durability.getInt());
            }
        });
        output.node("amount").set(Integer.class, Math.max(1, input.node("AMOUNT").getInt()));
        output.node("slot").set(Integer.class, convertSlot(input.node("POSITION-X").getInt(), input.node("POSITION-Y").getInt()));
        input.node("COLOR").act(color -> {
            if (!color.empty()) {
                output.node("rgb").set(String.class, color.getString().replace(" ", ""));
            }
        });
        input.node("NAME").act(name -> {
            if (!name.empty()) {
                output.node("display_name").set(String.class, convertPlaceholders(name.getString()));
            }
        });
        input.node("LORE").act(lore -> {
            if (!lore.empty()) {
                output.node("lore").setList(String.class, lore.getList(String.class).stream().map(this::convertPlaceholders).collect(Collectors.toList()));
            }
        });
        input.node("BANNER-PATTERNS").act(patterns -> {
           if (!patterns.empty()) {
               final var list = patterns.getList(String.class).stream()
                       .map(String::toUpperCase)
                       .map(it -> {
                           final var parts = it.split(":");
                           return (parts.length == 2) ? parts[1] + ';' + parts[0] : null;
                       })
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
               output.node("banner_meta").setList(String.class, list);
           }
        });
        input.node("ACTIONS").act(actions -> {
            if (!actions.empty()) {
                output.node("click_actions").setList(String.class, convertActions(actions.getList(String.class)));
            }
        });
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
