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

package me.gabytm.converter.utils;

import me.gabytm.converter.Converter;
import org.bukkit.plugin.java.JavaPlugin;

public enum Messages {
    NO_PERMISSION("&8[&4Converter&8] &cYou don't have the permission to use this command!"),
    CHESTCOMMANDS_MENU_NOT_FOUND("&8[&4Converter&8] &cA menu named &7'&f{menu}&7' &cwas not found."),
    CONVERTION_DONE("&8[&2Converter&8] &aDone! &7({duration}ms)"),
    CONVERTION_ERROR("&8[&4Converter&8] &cSomething went wrong, please check the console."),
    HELP(" \n&6Convertor &ev{version} &fby &6GabyTM\n" +
            " \n" +
            "  &6ChestCommands &7to\n" +
            "  &7- &6DeluxeMenus &e(menu)\n" +
            " \n" +
            "  &6QuickSell &7to\n" +
            "  &7- &6AutoSell\n" +
            " \n" +
            "&fUsage: &6/convert [from] [to] &e(arguments)"),
    INCORRECT_USAGE("&8[&4Converter&8] &cIncorrect usage. Type &f/converter help &cfor help."),
    QUICKSELL_NO_SHOPS_SECTION("&8[&4Converter&8] &cI could not locate the shops section in the QuickSell config!"),
    UNKNOWN_COMMAND("&8[&4Converter&8] &cUnknown command. Type &f/converter help &cfor help.");

    private final String message;
    private final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(Converter.class);

    Messages(final String message) { this.message = message; }

    public String getMessage() {
        return StringUtil.color(message.replace("{version}", PLUGIN.getDescription().getVersion()));
    }

    public String ccFormat(final String menu) {
        return StringUtil.color(message.replace("{menu}", menu));
    }

    public String format(final long duration) {
        return StringUtil.color(message.replace("{duration}", Long.toString(duration)));
    }
}