package me.kosinkadink.performantplants.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerHelper {

    public static boolean hasMissingFood(Player player) {
        return player.getFoodLevel() < 20;
    }

    public static boolean isAlive(Player player) {
        return player.getHealth() > 0;
    }

    public static EquipmentSlot oppositeHand(EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND) {
            return EquipmentSlot.OFF_HAND;
        }
        return EquipmentSlot.HAND;
    }

}
