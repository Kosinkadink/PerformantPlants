package me.kosinkadink.performantplants.settings;

import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.util.EnchantmentLevel;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class ItemSettings {

    private Material material;
    private String displayName;
    private List<String> lore;
    private String skullTexture;
    private int amount = 1;

    private final List<EnchantmentLevel> enchantments = new ArrayList<>();
    private final List<ItemFlag> itemFlags = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();

    private Color potionColor;
    private PotionData potionData;
    private int damage = 0;
    private boolean unbreakable = false;
    private Integer customModelData = null;

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

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public void addEnchantmentLevel(EnchantmentLevel enchantmentLevel) {
        enchantments.add(enchantmentLevel);
    }

    public void addItemFlag(ItemFlag flag) {
        itemFlags.add(flag);
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        potionEffects.add(potionEffect);
    }

    public Color getPotionColor() {
        return potionColor;
    }

    public void setPotionColor(Color potionColor) {
        this.potionColor = potionColor;
    }

    public PotionData getPotionData() {
        return potionData;
    }

    public void setPotionData(PotionData potionData) {
        this.potionData = potionData;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
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
        ItemBuilder builder = new ItemBuilder(getMaterial())
                .displayName(getDisplayName())
                .lore(getLore())
                .skullTexture(getSkullTexture())
                .damage(getDamage())
                .unbreakable(isUnbreakable())
                .amount(getAmount());
        // add all enchantments
        for (EnchantmentLevel enchantment : enchantments) {
            builder.addEnchantment(enchantment.getEnchantment(), enchantment.getLevel());
        }
        // add all potion effects
        for (PotionEffect potionEffect : potionEffects) {
            builder.addPotionEffect(potionEffect);
        }
        // add potion color, if set
        if (getPotionColor() != null) {
            builder.potionColor(getPotionColor());
        }
        // add potion data, if set
        if (getPotionData() != null) {
            builder.basePotionData(getPotionData());
        }
        // add item flags

        if (!itemFlags.isEmpty()) {
            builder.addItemFlags((ItemFlag[]) itemFlags.toArray());
        }
        // add custom model data
        if (getCustomModelData() != null) {
            builder.customModelData(getCustomModelData());
        }
        return builder.build();
    }

    public ItemStack generatePlantItemStack(String finalDisplayName, boolean isSeed) {
        ItemStack generatedStack = generateItemStack();
        if (getDisplayName() != null) {
            finalDisplayName = getDisplayName();
        } else if (isSeed) {
            finalDisplayName += " Seed";
        }
        return new PlantItemBuilder(generatedStack)
                .displayName(finalDisplayName)
                .build();
    }

    public ItemStack generatePlantItemStack(String finalDisplayName) {
        return generatePlantItemStack(finalDisplayName, false);
    }

}
