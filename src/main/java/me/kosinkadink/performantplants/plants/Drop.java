package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.inventory.ItemStack;

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

    public int getAmountValue(ExecutionContext context) {
        return amount.loadValue(context).getIntegerValue();
    }

    public ScriptBlock getDoIf() {
        return doIf;
    }

    public boolean isDoIf(ExecutionContext context) {
        return doIf.loadValue(context).getBooleanValue();
    }

    public ItemStack generateDrop(ExecutionContext context) {
        ItemStack dropStack = itemStack.clone();
        // if doIf true, drop amount
        if (isDoIf(context)) {
            int amount = Math.max(0, getAmountValue(context));
            dropStack.setAmount(amount);
            return dropStack;
        }
        // otherwise drop zero
        dropStack.setAmount(0);
        return dropStack;
    }
}
