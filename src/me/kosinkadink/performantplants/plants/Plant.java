package me.kosinkadink.performantplants.plants;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Plant {

    private String name;
    private String displayName;
    private String id;
    private ItemStack plantItem;

    public Plant(String name, String id, ItemStack itemStack) {
        this.name = name;
        displayName = ChatColor.LIGHT_PURPLE + name + ChatColor.RESET;
        this.id = id;
        plantItem = itemStack;
        updateItemDisplayName();
    }

    void updateItemDisplayName() {
        ItemMeta itemMeta = plantItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            plantItem.setItemMeta(itemMeta);
        }
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return plantItem;
    }

    public ItemStack getClonedItem() {
        return plantItem.clone();
    }

    public boolean isSimilar(ItemStack itemStack) {
        return plantItem.isSimilar(itemStack);
    }

}
