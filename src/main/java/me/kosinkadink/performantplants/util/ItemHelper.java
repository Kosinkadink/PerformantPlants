package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

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
            boolean isBasePlant = PerformantPlants.getInstance().getPlantTypeManager().isPlantItemStack(base);
            boolean isCheckedPlant = PerformantPlants.getInstance().getPlantTypeManager().isPlantItemStack(checked);
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

    public static PotionMeta getPotionMeta(ItemStack stack) {
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            return (PotionMeta) itemMeta;
        }
        return null;
    }

    public static boolean isMaterialWearableWithRightClick(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            // wearable on head
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case NETHERITE_HELMET:
            case TURTLE_HELMET:
            // wearable on chest
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
            case ELYTRA:
            // wearable on legs
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
            case NETHERITE_LEGGINGS:
            // wearable on feet
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
            case NETHERITE_BOOTS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialWearable(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            // wearable on head
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case NETHERITE_HELMET:
            case TURTLE_HELMET:
            // heads/skulls
            case PLAYER_HEAD:
            case CREEPER_HEAD:
            case DRAGON_HEAD:
            case ZOMBIE_HEAD:
            case SKELETON_SKULL:
            case WITHER_SKELETON_SKULL:
            // wearable on chest
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
            case ELYTRA:
            // wearable on legs
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
            case NETHERITE_LEGGINGS:
            // wearable on feet
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
            case NETHERITE_BOOTS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialPickaxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case NETHERITE_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialAxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case NETHERITE_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialShovel(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            case STONE_SHOVEL:
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case NETHERITE_SHOVEL:
            case WOODEN_SHOVEL:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialHoe(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case NETHERITE_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMaterialSword(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        switch (stack.getType()) {
            case STONE_SWORD:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case NETHERITE_SWORD:
            case WOODEN_SWORD:
                return true;
            default:
                return false;
        }
    }

    public static boolean decrementItemStack(ItemStack itemStack) {
        // if material is air, do nothing
        if (itemStack.getType() == Material.AIR) {
            return false;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        return true;
    }

}
