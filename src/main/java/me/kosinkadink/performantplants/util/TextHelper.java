package me.kosinkadink.performantplants.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextHelper {

    public static String translateAlternateColorCodes(char colorChar, String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes(colorChar, string);
    }

    public static String translateAlternateColorCodes(String string) {
        return translateAlternateColorCodes('&', string);
    }

    public static List<String> translateAlternateColorCodes(char colorChar, List<String> strings) {
        ArrayList<String> translatedStrings = new ArrayList<>();
        if (strings == null || strings.isEmpty()) {
            return translatedStrings;
        }
        for (String string : strings) {
            translatedStrings.add(ChatColor.translateAlternateColorCodes(colorChar, string));
        }
        return translatedStrings;
    }

    public static List<String> translateAlternateColorCodes(List<String> strings) {
        return translateAlternateColorCodes('&', strings);
    }

}
