package me.kosinkadink.performantplants.plants;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class Drop {

    private ItemStack itemStack;
    private int min;
    private int max;
    private double chance;

    public Drop(ItemStack itemStack, int min, int max, double chance) {
        this.itemStack = itemStack;
        this.min = min;
        this.max = max;
        this.chance = chance;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getChance() {
        return chance;
    }

    public ItemStack generateDrop() {
        ItemStack dropStack = itemStack.clone();
        // if random double succeeds chance, drop actual amount
        if (ThreadLocalRandom.current().nextDouble() <= chance/100.0) {
            if (min == max) {
                dropStack.setAmount(min);
            } else {
                dropStack.setAmount(ThreadLocalRandom.current().nextInt(min, max + 1));
            }
            return dropStack;
        }
        // otherwise drop zero
        dropStack.setAmount(0);
        return dropStack;
    }
}
