package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemHelper {

    public static String getDisplayName(ItemStack stack) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName()) {
                return itemMeta.getDisplayName();
            }
        }
        return "";
    }

    public static void setDisplayName(ItemStack stack, String displayName) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
        }
    }

    public static boolean checkIfMatches(ItemStack base, ItemStack checked) {
        if (checked == null) {
            return false;
        }
        if (base.getItemMeta() instanceof Damageable && getDamage(base) == 0) { // && !PlantItemBuilder.isPlantName(checked)) {
            boolean isBasePlant = Main.getInstance().getPlantTypeManager().isPlantItemStack(base);
            boolean isCheckedPlant = Main.getInstance().getPlantTypeManager().isPlantItemStack(checked);
            if (isBasePlant != isCheckedPlant) {
                return false;
            }
            if (!isCheckedPlant) {
                return checked.getType() == base.getType() &&
                        checked.getAmount() >= base.getAmount();
            }
            // repair a clone of checked item
            ItemStack repairedChecked = checked.clone();
            setDamage(repairedChecked, 0);
        }
        return checked.isSimilar(base);
    }

    public static boolean inventoryContains(Inventory inventory, ItemStack base) {
        if (base.getItemMeta() instanceof Damageable) {
            int amount = 0;
            for (ItemStack checked : inventory) {
                if (checkIfMatches(base, checked)) {
                    amount += checked.getAmount();
                }
                if (amount >= base.getAmount()) {
                    return true;
                }
            }
            return false;
        } else {
            return inventory.containsAtLeast(base, base.getAmount());
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

    public static void setDamage(ItemStack stack, int amount) {
        ItemMeta itemMeta = stack.getItemMeta();
        boolean destroyed = false;
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            damageable.setDamage(Math.max(0, amount));
            if (damageable.getDamage() >= stack.getType().getMaxDurability()) {
                destroyed = true;
            }
            stack.setItemMeta((ItemMeta) damageable);
        }
        if (destroyed) {
            stack.setAmount(stack.getAmount() - 1);
        }
    }

    public static int getDamage(ItemStack stack) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            return damageable.getDamage();
        }
        return 0;
    }

}
