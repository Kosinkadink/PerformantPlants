package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerHelper {

    public static boolean hasMissingFood(Player player) {
        return player.getFoodLevel() < 20;
    }

    public static boolean isAlive(Player player) {
        return player.getHealth() > 0;
    }

    public static OfflinePlayer refreshOfflinePlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer == null) {
            return null;
        }
        return PerformantPlants.getInstance().getServer().getOfflinePlayer(offlinePlayer.getUniqueId());
    }

    public static Player getFreshPlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer != null) {
            return refreshOfflinePlayer(offlinePlayer).getPlayer();
        }
        return null;
    }

    public static EquipmentSlot oppositeHand(EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND) {
            return EquipmentSlot.OFF_HAND;
        }
        return EquipmentSlot.HAND;
    }

}
