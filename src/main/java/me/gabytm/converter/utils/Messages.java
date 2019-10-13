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

package me.gabytm.converter.utils;

public enum Messages {
    NO_PERMISSION("&8[&4Converter&8] &cYou don't have the permission to use this command!"),
    CHESTCOMMANDS_MENU_NOT_FOUND("&8[&4Converter&8] &cA menu named &7'&f%menu%&7' &cwas not found."),
    CHESTCOMMANDS_DELUXEMENUS_USAGE("&8[&4Converter&8] &cUsage: &f/converter ChestCommands DeluxeMenus [menu]"),
    CONVERTION_DONE("&8[&2Converter&8] &aDone! &7(%duration%ms)"),
    CONVERTION_ERROR("&8[&4Converter&8] &cSomething went wrong, please check the console."),
    QUICKSELL_NO_SHOPS_SECTION("&8[&4Converter&8] &cI could not locate the shops section in the QuickSell config!"),
    UNKNOWN_COMMAND("&8[&4Converter&8] &cUnknown command. Type &f/converter help &cfor help.");

    private String value;

    Messages(String v) { this.value = v; }

    public String value() {
        return StringUtils.colorize(value);
    }
    public String ccFormat(String menu) {
        return StringUtils.colorize(value.replaceAll("%menu%", menu));
    }
    public String format(Long duration) {
        return StringUtils.colorize(value.replace("%duration%", duration.toString()));
    }
}
