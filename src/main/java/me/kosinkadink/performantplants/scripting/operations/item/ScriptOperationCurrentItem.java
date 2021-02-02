package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationCurrentItem extends ScriptOperationItem {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isItemStackSet()) {
            return new ScriptResult(context.getItemStack());
        }
        return ScriptResult.AIR;
    }

    @Override
    protected void setType() {
        type = ScriptType.ITEMSTACK;
    }
}
