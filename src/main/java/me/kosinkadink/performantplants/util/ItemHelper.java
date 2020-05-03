package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

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

    public static boolean checkIfMatches(ItemStack base, ItemStack checked) {
        if (base.getItemMeta() instanceof Damageable && !PlantItemBuilder.isPlantName(checked)) {
            return checked.getType() == base.getType() &&
                    checked.getAmount() >= base.getAmount();
        } else {
            return checked.isSimilar(base);
        }
    }

    public static boolean checkContainsEnchantments(ItemStack base, ItemStack checked, boolean matchLevel) {
        for (Map.Entry<Enchantment,Integer> entry : base.getEnchantments().entrySet()) {
            if (matchLevel) {
                if (checked.getEnchantmentLevel(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            } else {
                if (!checked.containsEnchantment(entry.getKey())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void updateDamage(ItemStack stack, int amount) {
        ItemMeta itemMeta = stack.getItemMeta();
        boolean destroyed = false;
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            damageable.setDamage(Math.max(0, damageable.getDamage() + amount));
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
