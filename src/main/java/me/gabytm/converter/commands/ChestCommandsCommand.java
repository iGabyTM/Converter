/*
 * Copyright 2020 GabyTM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *	permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.gabytm.converter.commands;

import me.gabytm.converter.Converter;
import me.gabytm.converter.utils.Messages;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command("convert")
public class ChestCommandsCommand extends CommandBase {
    private final Converter plugin;
    private final FileConfiguration config;
    private final Pattern matDataAmtPattern = Pattern.compile("([\\w\\s]+):([0-9\\s]+),([0-9\\s]+)");
    private final Pattern matAmtPattern = Pattern.compile("([\\w\\s]+),([0-9\\s]+)");
    private final Pattern matDataPattern = Pattern.compile("([\\w\\s]+):([0-9\\s]+)");

    public ChestCommandsCommand(Converter plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
    }

    @CompleteFor("chestcommands")
    public List<String> completion(final List<String> args) {
        if (args.size() == 1) {
            final List<String> pluginTo = Arrays.asList("DeluxeMenus", "DM");
            final List<String> completion = new ArrayList<>();

            StringUtil.copyPartialMatches(args.get(0), pluginTo, completion);
            Collections.sort(completion);
            return completion;
        }

        if (args.size() == 2) {
            final File menusDirectory = new File("plugins/ChestCommands/menu");

            if (!menusDirectory.exists()) {
                return Collections.emptyList();
            }

            final File[] files = menusDirectory.listFiles();

            if (files == null) {
                return Collections.emptyList();
            }

            final List<String> menus = Arrays.stream(files).map(File::getName).filter(name -> name.endsWith(".yml")).collect(Collectors.toList());
            final List<String> completion = new ArrayList<>();

            StringUtil.copyPartialMatches(args.get(1), menus, completion);
            Collections.sort(completion);
            return completion;
        }

        return Collections.emptyList();
    }

    @SubCommand("chestcommands")
    @Alias({"ChestCommands", "cc", "CC"})
    @Permission("convertor.access")
    @SuppressWarnings("SpellCheckingInspection")
    public void onCommand(final CommandSender sender, final String pluginTo, final String menu) {
        if (!pluginTo.equalsIgnoreCase("DeluxeMenus") && !pluginTo.equalsIgnoreCase("DM")) {
            sender.sendMessage(Messages.INCORRECT_USAGE.getMessage());
            return;
        }

        final String menuName = menu.toLowerCase().endsWith(".yml") ? menu : menu + ".yml";
        final long startTime = System.currentTimeMillis();
        final FileConfiguration ccConfig = YamlConfiguration.loadConfiguration(new File("plugins/ChestCommands/menu/" + menuName));

        if (!ccConfig.isConfigurationSection("menu-settings")) {
            sender.sendMessage(Messages.CHESTCOMMANDS_MENU_NOT_FOUND.ccFormat(menuName));
            return;
        }

        try {
            plugin.emptyConfig();
            plugin.saveConfig();
            config.set("menu_title", formatText(ccConfig.getString("menu-settings.name", "Menu " + menuName)));

            if (!ccConfig.isSet("menu-settings.command")) {
                config.set("open_command", menuName.replace(".yml", "") + "gui");
            } else if (!ccConfig.getString("menu-settings.command").contains(";")) {
                config.set("open_command", ccConfig.getString("menu-settings.command", menuName.replace(".yml", "") + "gui"));
            } else {
                config.set("open_command", new ArrayList<>(Arrays.asList(ccConfig.getString("menu-settings.command").split(";"))));
            }

            if (ccConfig.isSet("menu-settings.open-action")) {
                final List<String> openCommands = new ArrayList<>();

                if (!ccConfig.getString("menu-settings.open-action").contains(";")) {
                    openCommands.add(dmActions(ccConfig.getString("menu-settings.open-action")));
                } else {
                    Arrays.stream(ccConfig.getString("menu-settings.open-action").split(";")).forEach(cmd -> openCommands.add(dmActions(cmd)));
                }

                config.set("open_commands", openCommands);
            }

            config.set("size", ccConfig.getInt("menu-settings.rows", 6) * 9);

            if (ccConfig.isSet("menu-settings.auto-refresh")) {
                config.set("update_interval", ccConfig.getInt("menu-settings.auto-refresh"));
            }

            config.createSection("items");

            for (String configSection : ccConfig.getKeys(false)) {
                if (!ccConfig.isConfigurationSection(configSection) || ccConfig.getConfigurationSection(configSection).getName().equals("menu-settings")) {
                    continue;
                }

                final ConfigurationSection cs = ccConfig.getConfigurationSection(configSection);
                final String key = cs.getName();

                config.createSection("items." + key);

                if (cs.isSet("ID")) {
                    String id = cs.getString("ID");
                    final Matcher matDataAmtMatcher = matDataAmtPattern.matcher(id);
                    final Matcher matAmtMatcher = matAmtPattern.matcher(id);
                    final Matcher matDataMatcher = matDataPattern.matcher(id);
                    String material;
                    int data;
                    int amount;

                    if (matDataAmtMatcher.find()) {
                        material = matDataAmtMatcher.group(1).toUpperCase().replace(" ", "_");
                        data = Integer.parseInt(matDataAmtMatcher.group(2).trim());
                        amount = Integer.parseInt(matDataAmtMatcher.group(3).trim());

                        config.set("items." + key + ".material", material);
                        config.set("items." + key + ".data", data);
                        config.set("items." + key + ".amount", amount);
                    } else if (matAmtMatcher.find()) {
                        material = matAmtMatcher.group(1).toUpperCase().replace(" ", "_");
                        amount = Integer.parseInt(matAmtMatcher.group(2).trim());

                        config.set("items." + key + ".material", material);
                        config.set("items." + key + ".amount", amount);
                    } else if (matDataMatcher.find()) {
                        material = matDataMatcher.group(1).toUpperCase().replace(" ", "_");
                        data = Integer.parseInt(matDataMatcher.group(2).trim());

                        config.set("items." + key + ".material", material);
                        config.set("items." + key + ".data", data);
                    } else {
                        config.set("items." + key + ".material", id.replace(" ", "_").toUpperCase());
                    }
                }

                if (cs.isSet("DATA-VALUE")) {
                    config.set("items." + key + ".data", cs.getInt("DATA-VALUE"));
                }

                if (cs.isSet("AMOUNT")) {
                    config.set("items." + key + ".amount", cs.getInt("AMOUNT"));
                }

                if (cs.isSet("SKULL-OWNER")) {
                    config.set("items." + key + ".material", "head;" + cs.getString("SKULL-OWNER"));
                    config.set("items." + key + ".data", null);
                }

                if (cs.isSet("COLOR")) {
                    config.set("items." + key + ".color", cs.getString("COLOR").replace(" ", ""));
                }

                if (cs.isSet("POSITION-X") && cs.isSet("POSITION-Y"))
                    config.set("items." + key + ".slot", dmSlot(cs.getInt("POSITION-X"), cs.getInt("POSITION-Y")));

                if (cs.isSet("VIEW_PERMISSION")) {
                    config.set("items." + key + ".priority", 1);
                    config.set("items." + key + ".view_requirement.requirements.permission.type", "has permission");
                    config.set("items." + key + ".view_requirement.requirements.permission.permission", cs.get("VIEW-PERMISSION"));
                }

                if (cs.isSet("NAME")) {
                    config.set("items." + key + ".display_name", formatText(cs.getString("NAME")));
                }

                if (cs.isSet("LORE")) {
                    config.set("items." + key + ".lore", cs.getStringList("LORE").stream().map(this::formatText).collect(Collectors.toList()));
                }

                if (cs.isSet("ENCHANTMENT")) {
                    final List<String> enchantments = new ArrayList<>();

                    if (cs.getString("ENCHANTMENT").contains(";") && cs.getString("ENCHANTMENT").contains(",")) {
                        for (String e : cs.getString("ENCHANTMENT").split(";")) {
                            final String[] enchantment = e.split(",");

                            enchantments.add(enchantment[0] + ";" + enchantment[1].replace(" ", ""));
                        }
                    } else if (cs.getString("ENCHANTMENT").contains(",")) {
                        final String[] enchantment = cs.getString("ENCHANTMENT").split(",");

                        enchantments.add(enchantment[0] + ";" + enchantment[1].replace(" ", ""));
                    } else {
                        enchantments.add(cs.getString("ENCHANTMENT") + ";" + 1);
                    }

                    config.set("items." + key + ".enchantments", enchantments);
                }

                final List<String> left_click_commands = new ArrayList<>();
                final List<String> right_click_commands = new ArrayList<>();

                if (cs.isSet("COMMAND")) {
                    if (!cs.getString("COMMAND").contains(";")) {
                        final String command = cs.getString("COMMAND");

                        left_click_commands.add(dmActions(command));
                        right_click_commands.add(dmActions(command));
                    } else {
                        for (String cmd : cs.getString("COMMAND").split(";")) {
                            left_click_commands.add(dmActions(cmd));
                            right_click_commands.add(dmActions(cmd));
                        }
                    }

                    config.set("items." + key + ".left_click_commands", left_click_commands);
                    config.set("items." + key + ".right_click_commands", right_click_commands);
                }

                if (cs.isSet("LEVELS")) {
                    int levels = cs.getInt("LEVELS");

                    left_click_commands.add("[console] exp give -" + levels + "L %player_name%");
                    right_click_commands.add("[console] exp give -" + levels + "L %player_name%");

                    longClickRequirements(config, key, "xp_levels", ">=", "%player_level%", levels);

                    config.set("items." + key + ".left_click_commands", left_click_commands);
                    config.set("items." + key + ".right_click_commands", right_click_commands);
                }

                if (cs.isSet("PERMISSION")) {
                    shortClickRequirements(config, key, "permission", "has permission", "permission", cs.getString("PERMISSION"));
                }

                if (cs.isSet("PRICE")) {
                    int price = cs.getInt("PRICE");

                    left_click_commands.add("[console] eco take %player_name% " + price);
                    right_click_commands.add("[console] eco take %player_name% " + price);

                    shortClickRequirements(config, key, "money", "has money", "amount", price);
                }

                if (!cs.isSet("KEEP-OPEN") || !cs.getBoolean("KEEP-OPEN")) {
                    left_click_commands.add("[close]");
                    right_click_commands.add("[close]");

                    config.set("items." + key + ".left_click_commands", left_click_commands);
                    config.set("items." + key + ".right_click_commands", right_click_commands);
                }

                if (cs.isSet("REQUIRED-ITEM")) {
                    final String requiredItem = cs.getString("REQUIRED-ITEM");
                    final Matcher matDataAmtMatcher = matDataAmtPattern.matcher(requiredItem);
                    final Matcher matAmtMatcher = matAmtPattern.matcher(requiredItem);
                    final Matcher matDataMatcher = matDataPattern.matcher(requiredItem);
                    String material;
                    int data;
                    int amount;
                    String input;

                    if (matDataAmtMatcher.find()) {
                        material = matDataAmtMatcher.group(1).toUpperCase().replace(" ", "_");
                        data = Integer.parseInt(matDataAmtMatcher.group(2).trim());
                        amount = Integer.parseInt(matDataAmtMatcher.group(3).trim());
                        input = "%checkitem_mat:" + material + ",amt:" + amount + ",data:" + data + "%";
                    } else if (matAmtMatcher.find()) {
                        material = matAmtMatcher.group(1).toUpperCase().replace(" ", "_");
                        amount = Integer.parseInt(matAmtMatcher.group(2).trim());
                        input = "%checkitem_mat:" + material + ",amt:" + amount + "%";
                    } else if (matDataMatcher.find()) {
                        material = matDataMatcher.group(1).toUpperCase().replace(" ", "_");
                        data = Integer.parseInt(matDataMatcher.group(2).trim());
                        input = "%checkitem_mat:" + material + ",data:" + data + "%";
                    } else {
                        material = requiredItem.toUpperCase().replace(" ", "_");
                        input = "%checkitem_mat:" + material + "%";
                    }

                    longClickRequirements(config, key, "item", "string equals ignorecase", input, "yes");
                }

                if (cs.isSet("PERMISSION-MESSAGE")) {
                    final List<String> left_deny_commands = new ArrayList<>();
                    final List<String> right_deny_commands = new ArrayList<>();
                    final String permissionMessage = cs.getString("PERMISSION-MESSAGE");

                    left_deny_commands.add("[message] " + formatText(permissionMessage));
                    right_deny_commands.add("[message] " + formatText(permissionMessage));

                    config.set("items." + key + ".left_click_requirement.deny_commands", left_deny_commands);
                    config.set("items." + key + ".right_click_requirement.deny_commands", right_deny_commands);
                }
            }

            plugin.saveConfig();
            sender.sendMessage(Messages.CONVERTION_DONE.format(System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.CONVERTION_ERROR.getMessage());
        }
    }

    /**
     * Sets the click requirement section
     *
     * @param config config
     * @param key    item id
     * @param id     requirement id
     * @param type   requirement type
     * @param input  input
     * @param output output
     */
    private void longClickRequirements(FileConfiguration config, String key, String id, String type, String input, Object output) {
        config.set("items." + key + ".left_click_requirement.requirements." + id + ".type", type);
        config.set("items." + key + ".left_click_requirement.requirements." + id + ".input", input);
        config.set("items." + key + ".left_click_requirement.requirements." + id + ".output", output);
        config.set("items." + key + ".right_click_requirement.requirements." + id + ".type", type);
        config.set("items." + key + ".right_click_requirement.requirements." + id + ".input", input);
        config.set("items." + key + ".right_click_requirement.requirements." + id + ".output", output);
    }

    /**
     * Sets the click requirement section
     *
     * @param config    config
     * @param key       item id
     * @param id        requirement id
     * @param type      requirement type
     * @param valueType output type
     * @param value     output
     */
    @SuppressWarnings("Duplicates")
    private void shortClickRequirements(FileConfiguration config, String key, String id, String type, String valueType, Object value) {
        config.set("items." + key + ".left_click_requirement.requirements." + id + ".type", type);
        config.set("items." + key + ".left_click_requirement.requirements." + id + "." + valueType, value);
        config.set("items." + key + ".right_click_requirement.requirements." + id + ".type", type);
        config.set("items." + key + ".right_click_requirement.requirements." + id + "." + valueType, value);
    }

    /**
     * Convert from ChestCommand slots system (x and y) to DeluxeMenus format
     *
     * @param x is the x value (1 - 9)
     * @param y is the y value (1 - 6)
     * @return a number that can be used on DeluxeMenus slot option
     */
    private int dmSlot(int x, int y) {
        return 9 * (y - 1) + x - 1;
    }

    /**
     * Format the text from ChestCommand by replacing certain placeholders/symbols
     *
     * @param input is the given string to be formatted
     * @return a string compatible with DeluxeMenus
     */
    private String formatText(String input) {
        final String[] placeholders = new String[]{
                "{max_players}", "{money}", "{online}", "{player}", "{world}",
                "<3", "[*]", "[**]", "[p]", "[v]", "[+]", "[++]", "[x]", "[/]",
                "[cross]", "[arrow_right]", "[arrow_left]", "[arrow_up]", "[arrow_down]"
        };

        final String[] value = new String[]{
                "%server_max_players%", "%vault_eco_balance%", "%server_online%", "%player_name%", "%player_world%",
                "❤", "★", "✹", "●", "✔", "♦", "✦", "█", "▌",
                "✠", "→", "←", "↑", "↓"
        };

        return StringUtils.replaceEach(input, placeholders, value);
    }

    /**
     * Convert the ChestCommands actions to DeluxeMenus
     *
     * @param action   is the action identifier
     * @param argument is the given string used after the action
     * @return the actions format used by DeluxeMenus
     */
    @SuppressWarnings("SpellCheckingInspection")
    private String formatActions(String action, String argument) {
        switch (action.toLowerCase().replace(" ", "")) {
            case "broadcast":
                return "[console] broadcast" + formatText(argument);
            case "console":
                return "[console]" + formatText(argument);
            case "give":
                return "[console] give %player_name%" + formatText(argument);
            case "givemoney":
                return "[console] eco give %player_name%" + argument;
            case "give-points":
                return "[console] points give %player_name%" + argument;
            case "op":
                return "[message] This is a replacement for 'op:" + formatText(argument) + "' of ChestCommands";
            case "open":
                return "[openguimenu]" + argument.replace(".yml", "");
            case "server":
                return "[connect]" + argument;
            case "sound":
                return "[sound]" + argument.split(",")[0];
            case "tell":
                return "[message]" + formatText(argument);
            default:
                return "[message] This is a replacement of an unknown action: '" + action + "'";
        }
    }

    /**
     * Check if the given string contains a : to decide if it's a player action or not
     *
     * @param string is the action format of ChestCommand
     * @return the actions format used by DeluxeMenus
     */
    private String dmActions(String string) {
        if (string.contains(":")) {
            String[] action = string.split(":", 2);
            return formatActions(action[0], action[1]);
        } else {
            return "[player]" + string;
        }
    }
}