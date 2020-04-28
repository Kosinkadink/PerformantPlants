package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.settings.ItemSettings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHelper {

    public static String getDisplayName(ItemStack stack) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta != null) {
            return itemMeta.getDisplayName();
        }
        return null;
    }

    public static void setDisplayName(ItemStack stack, String displayName) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
        }
    }

    public static void updateDamage(ItemStack stack, int amount) {
        ItemMeta itemMeta = stack.getItemMeta();
        boolean destroyed = false;
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            damageable.setDamage(damageable.getDamage() + amount);
            if (damageable.getDamage() >= stack.getType().getMaxDurability()) {
                destroyed = true;
            }
            stack.setItemMeta((ItemMeta) damageable);
        }
        if (destroyed) {
            stack.setAmount(stack.getAmount() - 1);
        }
    }

}
