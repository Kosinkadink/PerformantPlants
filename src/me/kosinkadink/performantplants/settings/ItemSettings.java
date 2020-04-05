package me.kosinkadink.performantplants.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemSettings {

    private Material material;
    private String displayName;
    private List<String> lore;
    private String skullTexture;
    private ItemStack itemStack;

    public ItemSettings(Material material, String displayName, List<String> lore, String skullTexture) {
        this.material = material;
        this.displayName = displayName;
        if (lore != null) {
            this.lore = lore;
        } else {
            this.lore = new ArrayList<>();
        }
        this.skullTexture = skullTexture;
    }

    public ItemSettings(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
