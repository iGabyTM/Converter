/*
 * Copyright 2020 GabyTM
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

package me.gabytm.converter;

import me.gabytm.converter.commands.*;
import me.gabytm.converter.utils.Messages;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Converter extends JavaPlugin {
    @Override
    public void onEnable() {
        final CommandManager commandManager = new CommandManager(this);

        saveDefaultConfig();

        commandManager.register(new ChestCommandsCommand(this));
        commandManager.register(new HelpCommand());
        commandManager.register(new QuickSellCommand(this));

        commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> sender.sendMessage(Messages.INCORRECT_USAGE.getMessage()));
        commandManager.getMessageHandler().register("cmd.no.exists", sender -> sender.sendMessage(Messages.UNKNOWN_COMMAND.getMessage()));
        commandManager.getMessageHandler().register("cmd.no.permission", sender -> sender.sendMessage(Messages.NO_PERMISSION.getMessage()));
    }

    public void emptyConfig() {
        getConfig().getKeys(false).forEach(k -> getConfig().get(k, null));
    }
}