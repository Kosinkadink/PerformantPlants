package me.kosinkadink.performantplants.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import org.bukkit.Bukkit;

public class PlaceholderHelper {

    public static String setVariablesAndPlaceholders(ExecutionContext context, String stringInput) {
        String formatted = stringInput;
        // replace bracket placeholders
        if (context.isPlayerSet()) {
            if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                formatted = PlaceholderAPI.setBracketPlaceholders(context.getPlayer(), formatted);
            }
        }
        // replace plant variables
        formatted = ScriptHelper.setVariables(context, formatted);
        // replace placeholders
        if (context.isPlayerSet()) {
            if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                formatted = PlaceholderAPI.setPlaceholders(context.getPlayer(), formatted);
            }
        }
        return formatted;
    }

}
