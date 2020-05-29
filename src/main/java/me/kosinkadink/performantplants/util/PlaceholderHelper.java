package me.kosinkadink.performantplants.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderHelper {

    public static String setVariablesAndPlaceholders(PlantBlock plantBlock, Player player, String stringInput) {
        String formatted = stringInput;
        if (plantBlock != null) {
            formatted = ScriptHelper.setVariables(plantBlock, formatted);
        }
        if (player != null) {
            if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                formatted = PlaceholderAPI.setPlaceholders(player, formatted);
            }
        }
        return formatted;
    }

}
