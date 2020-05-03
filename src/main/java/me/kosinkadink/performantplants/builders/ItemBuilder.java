package me.kosinkadink.performantplants.builders;

import me.kosinkadink.performantplants.util.ReflectionHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class ItemBuilder {

    protected ItemStack item;

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

    public ItemBuilder material(Material material) {
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

    public ItemBuilder addLore(List<String> lore) {
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

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder addPotionEffect(PotionEffect potionEffect) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta.addCustomEffect(potionEffect, false);
            item.setItemMeta(potionMeta);
        }
        return this;
    }

    public ItemBuilder potionColor(Color color) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta.setColor(color);
            item.setItemMeta(potionMeta);
        }
        return this;
    }

    public ItemBuilder basePotionData(PotionData potionData) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta.setBasePotionData(potionData);
            item.setItemMeta(potionMeta);
        }
        return this;
    }

    public ItemBuilder damage(int amount) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            damageable.setDamage(amount);
            item.setItemMeta((ItemMeta) damageable);
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder skullTexture(String encodedUrl) {
        ReflectionHelper.setSkullTexture(item, encodedUrl);
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
