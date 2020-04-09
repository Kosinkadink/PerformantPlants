package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.settings.ItemSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
