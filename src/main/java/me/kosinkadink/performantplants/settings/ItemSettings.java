package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemSettings {

    private Material material;
    private String displayName;
    private List<String> lore;
    private String skullTexture;
    private int amount = 1;
    private ItemStack itemStack;

    public ItemSettings(Material material, String displayName, List<String> lore, String skullTexture, int amount) {
        this.material = material;
        this.displayName = displayName;
        if (lore != null) {
            this.lore = lore;
        } else {
            this.lore = new ArrayList<>();
        }
        this.skullTexture = skullTexture;
        setAmount(amount);
    }

    public ItemSettings(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        if (material != null) {
            this.material = material;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null) {
            this.displayName = displayName;
        }
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        if (lore != null) {
            this.lore = lore;
        }
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        if (skullTexture != null) {
            this.skullTexture = skullTexture;
        }
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount > 0) {
            this.amount = amount;
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack generateItemStack() {
        if (getItemStack() != null) {
            return getItemStack();
        }
        return new ItemBuilder(getMaterial())
                .displayName(getDisplayName())
                .lore(getLore())
                .skullTexture(getSkullTexture())
                .amount(getAmount())
                .build();
    }

}
