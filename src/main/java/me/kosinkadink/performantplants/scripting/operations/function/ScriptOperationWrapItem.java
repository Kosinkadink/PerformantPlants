package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationWrapItem extends ScriptOperation {

    public ScriptOperationWrapItem(ScriptBlock item, ScriptBlock scriptBlock) {
        super(item, scriptBlock);
    }

    public ScriptBlock getItem() {
        return inputs[0];
    }

    public ScriptBlock getScriptBlock() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ItemStack oldItemStack = context.getItemStack();
        // set context item stack
        ItemStack itemStack = getItem().loadValue(context).getItemStackValue();
        context.set(itemStack);
        ScriptResult result = getScriptBlock().loadValue(context);
        context.set(oldItemStack);
        return result;
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isItemStack(getItem())) {
            throw new IllegalArgumentException("ScriptOperationWrapItem requires ScriptType ITEMSTACK for item block");
        }
    }

}
