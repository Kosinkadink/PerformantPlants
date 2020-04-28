package me.kosinkadink.performantplants.util;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentLevel {

    private Enchantment enchantment;
    private int level;

    public EnchantmentLevel(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
