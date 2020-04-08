package me.kosinkadink.performantplants.builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlantItemBuilder extends ItemBuilder {

    static String prefix = String.format("%s%s%s",ChatColor.LIGHT_PURPLE, ChatColor.RESET, ChatColor.LIGHT_PURPLE);
    static String postfix = String.format("%s%s%s",ChatColor.RESET, ChatColor.LIGHT_PURPLE, ChatColor.RESET);

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
            // add prefix/postfix if not already present
            if (!name.startsWith(prefix) || !name.endsWith(postfix)) {
                itemMeta.setDisplayName(prefix + name + postfix);
                item.setItemMeta(itemMeta);
            }
        }
        return this;
    }

}
