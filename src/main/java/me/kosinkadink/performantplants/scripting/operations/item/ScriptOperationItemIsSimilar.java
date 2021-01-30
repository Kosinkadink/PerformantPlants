package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationItemIsSimilar extends ScriptOperationItem {

    private final ItemStack itemStack;

    public ScriptOperationItemIsSimilar(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (!context.isItemStackSet()) {
            return ScriptResult.FALSE;
        }
        // check if item is similar
        return new ScriptResult(context.getItemStack().isSimilar(itemStack));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
