package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class Drop {

    private ItemStack itemStack;
    private ScriptBlock min;
    private ScriptBlock max;
    private ScriptBlock doIf;

    public Drop(ItemStack itemStack, ScriptBlock min, ScriptBlock max, ScriptBlock doIf) {
        this.itemStack = itemStack;
        this.min = min;
        this.max = max;
        this.doIf = doIf;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ScriptBlock getMin() {
        return min;
    }

    public int getMinValue(Player player, PlantBlock plantBlock) {
        return min.loadValue(plantBlock, player).getIntegerValue();
    }

    public ScriptBlock getMax() {
        return max;
    }

    public int getMaxValue(Player player, PlantBlock plantBlock) {
        return max.loadValue(plantBlock, player).getIntegerValue();
    }

    public ScriptBlock getDoIf() {
        return doIf;
    }

    public boolean isDoIf(Player player, PlantBlock plantBlock) {
        return doIf.loadValue(plantBlock, player).getBooleanValue();
    }

    public ItemStack generateDrop(Player player, PlantBlock plantBlock) {
        ItemStack dropStack = itemStack.clone();
        // if doIf true, drop amount
        if (isDoIf(player, plantBlock)) {
            int min = Math.max(0, getMinValue(player, plantBlock));
            int max = Math.max(0, getMaxValue(player, plantBlock));
            if (min == max) {
                dropStack.setAmount(min);
            } else if (min < max) {
                dropStack.setAmount(ThreadLocalRandom.current().nextInt(min, max + 1));
            } else {
                dropStack.setAmount(ThreadLocalRandom.current().nextInt(max, min + 1));
            }
            return dropStack;
        }
        // otherwise drop zero
        dropStack.setAmount(0);
        return dropStack;
    }
}
