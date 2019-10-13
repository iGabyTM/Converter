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
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;

import static me.gabytm.converter.utils.StringUtils.colorize;

@Command("convert")
public class QuickSellCommand extends CommandBase {
    private Converter plugin;

    public QuickSellCommand(Converter plugin) { this.plugin = plugin; }

    @SubCommand("quicksell")
    @Alias("qs")
    public void onCommand(CommandSender sender, String[] args) {
        if (sender.hasPermission("convertor.access")) {
            FileConfiguration config = plugin.getConfig();

            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("AutoSell") || args[1].equalsIgnoreCase("AS")) {
                    FileConfiguration qsConfig = YamlConfiguration.loadConfiguration(new File("plugins/QuickSell/config.yml"));
                    long startTime = System.currentTimeMillis();

                    if (qsConfig.isConfigurationSection("shops")) {
                        try {
                            for (String key : plugin.getConfig().getKeys(false)) {
                                plugin.getConfig().set(key, null);
                            }

                            plugin.saveConfig();
                            config.createSection("shops");

                            int priority = qsConfig.getConfigurationSection("shops").getKeys(false).size() + 1;

                            for (String key : qsConfig.getConfigurationSection("shops").getKeys(false)) {
                                ArrayList<String> shopItems = new ArrayList<>();

                                priority--;
                                config.set("shops." + key + ".priority", priority);

                                if (qsConfig.isConfigurationSection("shops." + key + ".price")) {
                                    for (String item : qsConfig.getConfigurationSection("shops." + key + ".price").getKeys(false)) {
                                        if (item.contains("-")) {
                                            String[] material = item.split("-");

                                            shopItems.add(material[0] + ";" + material[1] + "," + qsConfig.getInt("shops." + key + ".price." + item));
                                        } else {
                                            shopItems.add(item + "," + qsConfig.getInt("shops." + key + ".price." + item));
                                        }
                                    }

                                    plugin.getLogger().info(colorize("&aThe shop '" + key + "' has been converted."));
                                } else {
                                    shopItems.add("");
                                    plugin.getLogger().info(colorize("&cThe shop '" + key + "' has no items."));
                                }

                                config.set("shops." + key + ".shop_items", shopItems);
                            }

                            plugin.saveConfig();
                            sender.sendMessage(Messages.CONVERTION_DONE.format(System.currentTimeMillis() - startTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage(Messages.CONVERTION_ERROR.value());
                        }
                    } else {
                        sender.sendMessage(Messages.QUICKSELL_NO_SHOPS_SECTION.value());
                    }
                } else {
                    sender.sendMessage(Messages.UNKNOWN_COMMAND.value());
                }
            } else {
                sender.sendMessage(Messages.UNKNOWN_COMMAND.value());
            }
        } else {
            sender.sendMessage(Messages.NO_PERMISSION.value());
        }
    }
}
