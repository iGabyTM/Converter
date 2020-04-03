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
import me.gabytm.converter.utils.StringUtil;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.gabytm.converter.utils.StringUtil.color;

@Command("convert")
public class QuickSellCommand extends CommandBase {
    private Converter plugin;
    private FileConfiguration config;

    public QuickSellCommand(Converter plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
    }

    @CompleteFor("quicksell")
    public List<String> completion(final List<String> args) {
        if (args.size() == 1) {
            final List<String> pluginTo = Arrays.asList("AutoSell", "AS");
            final List<String> completion = new ArrayList<>();

            org.bukkit.util.StringUtil.copyPartialMatches(args.get(0), pluginTo, completion);
            Collections.sort(completion);
            return completion;
        }

        return Collections.emptyList();
    }

    @SubCommand("quicksell")
    @Alias({ "QuickSell", "qs", "QS" })
    @Permission("convertor.access")
    public void onCommand(final CommandSender sender, final String pluginTo) {
        if (!pluginTo.equalsIgnoreCase("AutoSell") && !pluginTo.equalsIgnoreCase("AS")) {
        	sender.sendMessage(Messages.INCORRECT_USAGE.getMessage());
        	return;
        }

        final FileConfiguration qsConfig = YamlConfiguration.loadConfiguration(new File("plugins/QuickSell/config.yml"));
        final long startTime = System.currentTimeMillis();

        if (!qsConfig.isConfigurationSection("shops")) {
        	sender.sendMessage(Messages.QUICKSELL_NO_SHOPS_SECTION.getMessage());
        	return;
        }

        try {
            plugin.emptyConfig();
            plugin.saveConfig();
            config.createSection("shops");

            int priority = qsConfig.getConfigurationSection("shops").getKeys(false).size() + 1;

            for (String key : qsConfig.getConfigurationSection("shops").getKeys(false)) {
                final List<String> shopItems = new ArrayList<>();

                priority--;
                config.set("shops." + key + ".priority", priority);

                if (!qsConfig.isConfigurationSection("shops." + key + ".price")) {
                	shopItems.add("");
                    plugin.getLogger().info(color("&cThe shop '" + key + "' has no items."));
                }

                for (String item : qsConfig.getConfigurationSection("shops." + key + ".price").getKeys(false)) {
                	if (!item.contains("-")) {
                		shopItems.add(item + "," + qsConfig.getInt("shops." + key + ".price." + item));
                		continue;
                	}

                    final String[] material = item.split("-");

                    shopItems.add(material[0] + ";" + material[1] + "," + qsConfig.getInt("shops." + key + ".price." + item));
                }

                plugin.getLogger().info(StringUtil.color("&aThe shop '" + key + "' has been converted."));
                config.set("shops." + key + ".shop_items", shopItems);
            }

            plugin.saveConfig();
            sender.sendMessage(Messages.CONVERTION_DONE.format(System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.CONVERTION_ERROR.getMessage());
        }
    }
}