package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.settings.ItemSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHelper {

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        // if same type, keep comparing
        if (stack1.getType() == stack2.getType()) {
            // if player heads, compare in a way that won't throw NullPointerException
            if (stack1.getType() == Material.PLAYER_HEAD) {
                ItemMeta stack1meta = stack1.getItemMeta();
                ItemMeta stack2meta = stack2.getItemMeta();
                if (stack1meta == null || stack2meta == null) {
                    return false;
                }
                return stack1meta.getDisplayName().equals(stack2meta.getDisplayName());
            }
            // otherwise use normal isSimilar
            return stack1.isSimilar(stack2);
        }
        return false;
    }

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

    public static ItemStack fromItemSettings(ItemSettings settings, String displayName, boolean isSeed) {
        if (settings.getDisplayName() != null) {
            displayName = settings.getDisplayName();
        } else if (isSeed) {
            displayName += " Seed";
        }
        return new PlantItemBuilder(settings.getMaterial())
                .displayName(displayName)
                .lore(settings.getLore())
                .skullTexture(settings.getSkullTexture())
                .build();
    }

    public static ItemStack fromItemSettings(ItemSettings settings, String displayName) {
        return fromItemSettings(settings, displayName, false);
    }

}
