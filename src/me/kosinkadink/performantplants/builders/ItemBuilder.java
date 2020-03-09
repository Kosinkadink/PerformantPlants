package me.kosinkadink.performantplants.builders;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {

    private ItemStack item;

    /**
     * @param material
     * Create ItemBuilder with selected material
     */
    public ItemBuilder(Material material) {
        item = new ItemStack(material);
    }

    /**
     * @param itemStack
     * Create ItemBuilder with selected itemStack
     */
    public ItemBuilder(ItemStack itemStack) {
        item = itemStack.clone();
    }

    public ItemBuilder itemMeta(ItemMeta itemMeta) {
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder type(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder displayName(String name) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder appendLore(List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasLore()) {
                List<String> presentLore = itemMeta.getLore();
                assert presentLore != null;
                presentLore.addAll(lore);
                itemMeta.setLore(presentLore);
            }
            else {
                itemMeta.setLore(lore);
            }
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
