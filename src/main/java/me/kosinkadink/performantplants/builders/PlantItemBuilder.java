package me.kosinkadink.performantplants.builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlantItemBuilder extends ItemBuilder {

    static String prefix = ""+ChatColor.LIGHT_PURPLE;
    static String postfix = ""+ChatColor.RESET;
    static String colorString = "" + ChatColor.COLOR_CHAR;


    public PlantItemBuilder(Material material) {
        super(material);
    }

    public PlantItemBuilder(ItemStack itemStack) {
        super(itemStack);
        if (itemStack.getItemMeta() != null) {
            displayName(itemStack.getItemMeta().getDisplayName());
        }
    }

    @Override
    public PlantItemBuilder displayName(String name) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(prefix + name + postfix);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public static boolean hasPlantPrefix(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            return itemMeta.getDisplayName().startsWith(colorString);
        }
        return false;
    }

}
