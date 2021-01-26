package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class Drop {

    private final ItemStack itemStack;
    private final ScriptBlock amount;
    private final ScriptBlock doIf;

    public Drop(ItemStack itemStack, ScriptBlock amount, ScriptBlock doIf) {
        this.itemStack = itemStack;
        this.amount = amount;
        this.doIf = doIf;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ScriptBlock getAmount() {
        return amount;
    }

    public int getAmountValue(Player player, PlantBlock plantBlock) {
        return amount.loadValue(plantBlock, player).getIntegerValue();
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
            int amount = Math.max(0, getAmountValue(player, plantBlock));
            dropStack.setAmount(amount);
            return dropStack;
        }
        // otherwise drop zero
        dropStack.setAmount(0);
        return dropStack;
    }
}
