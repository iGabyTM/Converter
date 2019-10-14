/*
 * Copyright 2019 GabyTM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command("convert")
public class ChestCommandsCommand extends CommandBase {
    private Converter plugin;
    private Pattern matDataAmtPattern = Pattern.compile("([\\w\\s]+):([0-9\\s]+),([0-9\\s]+)");
    private Pattern matAmtPattern = Pattern.compile("([\\w\\s]+),([0-9\\s]+)");
    private Pattern matDataPattern = Pattern.compile("([\\w\\s]+):([0-9\\s]+)");

    public ChestCommandsCommand(Converter plugin) { this.plugin = plugin; }

    @SubCommand("chestcommands")
    @Alias("cc")
    @Permission("convertor.access")
    public void onCommand(CommandSender sender, String pluginTo, String menu) {
        FileConfiguration config = plugin.getConfig();

        if (pluginTo.equalsIgnoreCase("DeluxeMenus1") || pluginTo.equalsIgnoreCase("DM")) {
            String menuName = menu.toLowerCase().endsWith(".yml") ? menu : menu + ".yml";
            long startTime = System.currentTimeMillis();
            FileConfiguration ccConfig = YamlConfiguration.loadConfiguration(new File("plugins/ChestCommands/menu/" + menuName));

            if (ccConfig.isConfigurationSection("menu-settings")) {
                try {
                    for (String key : plugin.getConfig().getKeys(false)) {
                        plugin.getConfig().set(key, null);
                    }

                    plugin.saveConfig();
                    config.set("menu_title", formatText(ccConfig.getString("menu-settings.name", "Menu " + menuName)));

                    if (ccConfig.isSet("menu-settings.command")) {
                        if (ccConfig.getString("menu-settings.command").contains(";")) {
                            List<String> open_command = new ArrayList<>(Arrays.asList(ccConfig.getString("menu-settings.command").split(";")));

                            config.set("open_command", open_command);
                        } else {
                            config.set("open_command", ccConfig.getString("menu-settings.command", menuName.replace(".yml", "") + "gui"));
                        }
                    } else {
                        config.set("open_command", menuName.replace(".yml", "") + "gui");
                    }

                    if (ccConfig.isSet("menu-settings.open-action")) {
                        List<String> open_commands = new ArrayList<>();

                        if (ccConfig.getString("menu-settings.open-action").contains(";")) {
                            for (String cmd : ccConfig.getString("menu-settings.open-action").split(";")) {
                                open_commands.add(dmActions(cmd));
                            }
                        } else {
                            open_commands.add(dmActions(ccConfig.getString("menu-settings.open-action")));
                        }

                        config.set("open_commands", open_commands);
                    }

                    config.set("size", ccConfig.getInt("menu-settings.rows", 6) * 9);

                    if (ccConfig.isSet("menu-settings.auto-refresh")) config.set("update_interval", ccConfig.getInt("menu-settings.auto-refresh"));

                    config.createSection("items");

                    for (String configSection : ccConfig.getKeys(false)) {
                        if (ccConfig.isConfigurationSection(configSection) && !ccConfig.getConfigurationSection(configSection).getName().equals("menu-settings")) {
                            ConfigurationSection cs = ccConfig.getConfigurationSection(configSection);
                            String key = cs.getName();

                            config.createSection("items." + key);

                            if (cs.isSet("ID")) {
                                String id = cs.getString("ID");
                                Matcher matDataAmtMatcher = matDataAmtPattern.matcher(id);
                                Matcher matAmtMatcher = matAmtPattern.matcher(id);
                                Matcher matDataMatcher = matDataPattern.matcher(id);

                                if (matDataAmtMatcher.find()) {
                                    String material = matDataAmtMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int data = Integer.parseInt(matDataAmtMatcher.group(2).trim());
                                    int amount = Integer.parseInt(matDataAmtMatcher.group(3).trim());

                                    config.set("items." + key + ".material", material);
                                    config.set("items." + key + ".data", data);
                                    config.set("items." + key + ".amount", amount);
                                } else if (matAmtMatcher.find()) {
                                    String material = matAmtMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int amount = Integer.parseInt(matAmtMatcher.group(2).trim());

                                    config.set("items." + key + ".material", material);
                                    config.set("items." + key + ".amount", amount);
                                } else if (matDataMatcher.find()) {
                                    String material = matDataMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int data = Integer.parseInt(matDataMatcher.group(2).trim());

                                    config.set("items." + key + ".material", material);
                                    config.set("items." + key + ".data", data);
                                } else {
                                    config.set("items." + key + ".material", id.replaceAll(" ", "_").toUpperCase());
                                }
                            }

                            if (cs.isSet("DATA-VALUE")) config.set("items." + key + ".data", cs.getInt("DATA-VALUE"));

                            if (cs.isSet("AMOUNT")) config.set("items." + key + ".amount", cs.getInt("AMOUNT"));

                            if (cs.isSet("SKULL-OWNER")) {
                                config.set("items." + key + ".material", "head;" + cs.getString("SKULL-OWNER"));
                                config.set("items." + key + ".data", null);
                            }

                            if (cs.isSet("COLOR")) config.set("items." + key + ".color", cs.getString("COLOR").replaceAll(" ", ""));

                            if (cs.isSet("POSITION-X") && cs.isSet("POSITION-Y")) config.set("items." + key + ".slot", dmSlot(cs.getInt("POSITION-X"), cs.getInt("POSITION-Y")));

                            if (cs.isSet("VIEW_PERMISSION")) {
                                config.set("items." + key + ".priority", 1);
                                config.set("items." + key + ".view_requirement.requirements.permission.type", "has permission");
                                config.set("items." + key + ".view_requirement.requirements.permission.permission", cs.get("VIEW-PERMISSION"));
                            }

                            if (cs.isSet("NAME")) config.set("items." + key + ".display_name", formatText(cs.getString("NAME")));

                            if (cs.isSet("LORE")) {
                                List<String> lore = new ArrayList<>();

                                for (String line : cs.getStringList("LORE")) {
                                    lore.add(formatText(line));
                                }

                                config.set("items." + key + ".lore", lore);
                            }

                            if (cs.isSet("ENCHANTMENT")) {
                                ArrayList<String> enchantments = new ArrayList<>();

                                if (cs.getString("ENCHANTMENT").contains(";") && cs.getString("ENCHANTMENT").contains(",")) {
                                    for (String e : cs.getString("ENCHANTMENT").split(";")) {
                                        String[] enchantment = e.split(",");

                                        enchantments.add(enchantment[0] + ";" + enchantment[1].replaceAll(" ", ""));
                                    }
                                } else if (cs.getString("ENCHANTMENT").contains(",")) {
                                    String[] enchantment = cs.getString("ENCHANTMENT").split(",");

                                    enchantments.add(enchantment[0] + ";" + enchantment[1].replaceAll(" ", ""));
                                } else {
                                    enchantments.add(cs.getString("ENCHANTMENT") + ";" + 1);
                                }

                                config.set("items." + key + ".enchantments", enchantments);
                            }

                            ArrayList<String> left_click_commands = new ArrayList<>();
                            ArrayList<String> right_click_commands = new ArrayList<>();

                            if (cs.isSet("COMMAND")) {
                                if (cs.getString("COMMAND").contains(";")) {
                                    for (String cmd : cs.getString("COMMAND").split(";")) {
                                        left_click_commands.add(dmActions(cmd));
                                        right_click_commands.add(dmActions(cmd));
                                    }
                                } else {
                                    String command = cs.getString("COMMAND");

                                    left_click_commands.add(dmActions(command));
                                    right_click_commands.add(dmActions(command));
                                }

                                config.set("items." + key + ".left_click_commands", left_click_commands);
                                config.set("items." + key + ".right_click_commands", right_click_commands);
                            }

                            if (cs.isSet("LEVELS")) {
                                int levels = cs.getInt("LEVELS");

                                left_click_commands.add("[console] exp give -" + levels + "L %player_name% ");
                                right_click_commands.add("[console] exp give -" + levels + "L %player_name% ");

                                config.set("items." + key + ".left_click_requirement.requirements.levels.type", ">=");
                                config.set("items." + key + ".left_click_requirement.requirements.levels.input", "%player_level%");
                                config.set("items." + key + ".left_click_requirement.requirements.levels.output", levels);
                                config.set("items." + key + ".right_click_requirement.requirements.levels.type", ">=");
                                config.set("items." + key + ".right_click_requirement.requirements.levels.input", "%player_level%");
                                config.set("items." + key + ".right_click_requirement.requirements.levels.output", levels);
                                config.set("items." + key + ".left_click_commands", left_click_commands);
                                config.set("items." + key + ".right_click_commands", right_click_commands);
                            }

                            if (cs.isSet("PERMISSION")) {
                                String permission = cs.getString("PERMISSION");

                                config.set("items." + key + ".left_click_requirement.requirements.permission.type", "has permission");
                                config.set("items." + key + ".left_click_requirement.requirements.permission.permission", permission);
                                config.set("items." + key + ".right_click_requirement.requirements.permission.type", "has permission");
                                config.set("items." + key + ".right_click_requirement.requirements.permission.permission", permission);
                            }

                            if (cs.isSet("PRICE")) {
                                int price = cs.getInt("PRICE");

                                left_click_commands.add("[console] eco take %player_name% " + price);
                                right_click_commands.add("[console] eco take %player_name% " + price);

                                config.set("items." + key + ".left_click_requirement.requirements.money.type", "has money");
                                config.set("items." + key + ".left_click_requirement.requirements.money.amount", price);
                                config.set("items." + key + ".right_click_requirement.requirements.money.type", "has money");
                                config.set("items." + key + ".right_click_requirement.requirements.money.amount", price);
                                config.set("items." + key + ".left_click_commands", left_click_commands);
                                config.set("items." + key + ".right_click_commands", right_click_commands);
                            }

                            if (!cs.isSet("KEEP-OPEN") || !cs.getBoolean("KEEP-OPEN")) {
                                left_click_commands.add("[close]");
                                right_click_commands.add("[close]");

                                config.set("items." + key + ".left_click_commands", left_click_commands);
                                config.set("items." + key + ".right_click_commands", right_click_commands);
                            }

                            if (cs.isSet("REQUIRED-ITEM")) {
                                String requiredItem = cs.getString("REQUIRED-ITEM");
                                Matcher matDataAmtMatcher = matDataAmtPattern.matcher(requiredItem);
                                Matcher matAmtMatcher = matAmtPattern.matcher(requiredItem);
                                Matcher matDataMatcher = matDataPattern.matcher(requiredItem);

                                if (matDataAmtMatcher.find()) {
                                    String material = matDataAmtMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int data = Integer.parseInt(matDataAmtMatcher.group(2).trim());
                                    int amount = Integer.parseInt(matDataAmtMatcher.group(3).trim());
                                    StringBuilder input = new StringBuilder().append("%checkitem_mat:").append(material).append(",amt:").append(amount).append(",data:").append(data).append("%");

                                    config.set("items." + key + ".left_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".left_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".left_click_requirement.requirements.item.output", "yes");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".right_click_requirement.requirements.item.output", "yes");
                                } else if (matAmtMatcher.find()) {
                                    String material = matAmtMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int amount = Integer.parseInt(matAmtMatcher.group(2).trim());
                                    StringBuilder input = new StringBuilder().append("%checkitem_mat:").append(material).append(",amt:").append(amount).append("%");

                                    System.out.println("matAmtMatcher.toString() = " + matAmtMatcher.toString());

                                    config.set("items." + key + ".left_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".left_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".left_click_requirement.requirements.item.output", "yes");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".right_click_requirement.requirements.item.output", "yes");
                                } else if (matDataMatcher.find()) {
                                    String material = matDataMatcher.group(1).toUpperCase().replaceAll(" ", "_");
                                    int data = Integer.parseInt(matDataMatcher.group(2).trim());
                                    StringBuilder input = new StringBuilder().append("%checkitem_mat:").append(material).append(",data:").append(data).append("%");

                                    config.set("items." + key + ".left_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".left_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".left_click_requirement.requirements.item.output", "yes");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".right_click_requirement.requirements.item.output", "yes");
                                } else {
                                    String material = requiredItem.toUpperCase().replaceAll(" ", "_");
                                    StringBuilder input = new StringBuilder().append("%checkitem_mat:").append(material).append("%");

                                    config.set("items." + key + ".left_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".left_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".left_click_requirement.requirements.item.output", "yes");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.type", "string equals ignorecase");
                                    config.set("items." + key + ".right_click_requirement.requirements.item.input", input.toString());
                                    config.set("items." + key + ".right_click_requirement.requirements.item.output", "yes");
                                }
                            }

                            if (cs.isSet("PERMISSION-MESSAGE")) {
                                ArrayList<String> left_deny_commands = new ArrayList<>();
                                ArrayList<String> right_deny_commands = new ArrayList<>();
                                String permissionMessage = cs.getString("PERMISSION-MESSAGE");

                                left_deny_commands.add("[message] " + formatText(permissionMessage));
                                right_deny_commands.add("[message] " + formatText(permissionMessage));

                                config.set("items." + key + ".left_click_requirement.deny_commands", left_deny_commands);
                                config.set("items." + key + ".right_click_requirement.deny_commands", right_deny_commands);
                            }
                        }
                    }

                    plugin.saveConfig();
                    sender.sendMessage(Messages.CONVERTION_DONE.format(System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(Messages.CONVERTION_ERROR.value());
                }
            } else {
                sender.sendMessage(Messages.CHESTCOMMANDS_MENU_NOT_FOUND.ccFormat(menuName));
            }
        }
    }

    /**
     * Convert from ChestCommand slots system (x and y) to DeluxeMenus format
     * @param x is the x value (1 - 9)
     * @param y is the y value (1 - 6)
     * @return a number that can be used on DeluxeMenus slot option
     */
    private int dmSlot(int x, int y) {
        return 9 * (y - 1) + x - 1;
    }

    /**
     * Format the text from ChestCommand by replacing certain placeholders/symbols
     * @param input is the given string to be formatted
     * @return a string compatible with DeluxeMenus
     */
    private String formatText(String input) {
        return input
                // Placeholders
                .replaceAll("(?i)\\{max_players}", "%server_max_players%")
                .replaceAll("(?i)\\{money}", "%vault_eco_balance%")
                .replaceAll("(?i)\\{online}", "%server_online%")
                .replaceAll("(?i)\\{player}", "%player_name%")
                .replaceAll("(?i)\\{world}", "%player_world%")
                // Symbols
                .replaceAll("(?i)<3", "❤")
                .replaceAll("(?i)\\[\\*]", "★")
                .replaceAll("(?i)\\[\\*\\*]", "✹")
                .replaceAll("(?i)\\[p]", "●")
                .replaceAll("(?i)\\[v]", "✔")
                .replaceAll("(?i)\\[\\+]", "♦")
                .replaceAll("(?i)\\[\\+\\+]", "✦")
                .replaceAll("(?i)\\[x]", "█")
                .replaceAll("(?i)\\[/]", "▌")
                .replaceAll("(?i)\\[cross]", "✠")
                .replaceAll("(?i)\\[arrow_right]", "→")
                .replaceAll("(?i)\\[arrow_left]", "←")
                .replaceAll("(?i)\\[arrow_up]", "↑")
                .replaceAll("(?i)\\[arrow_down]", "↓");
    }

    /**
     * Convert the ChestCommands actions to DeluxeMenus
     * @param action is the action identifier
     * @param argument is the given string used after the action
     * @return the actions format used by DeluxeMenus
     */
    private String formatActions(String action, String argument) {
        switch (action.toLowerCase().replaceAll(" ", "")) {
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
                return "[message] This is a replacement for 'op:" + formatText(argument) +"' of ChestCommands";
            case "open":
                return "[openguimenu]" + argument.replaceAll("(?i).yml", "");
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