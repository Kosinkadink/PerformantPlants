package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationGetChestplate extends ScriptOperationInventory {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            return new ScriptResult(context.getPlayer().getInventory().getChestplate());
        }
        return ScriptResult.AIR;
    }

    @Override
    protected void setType() {
        type = ScriptType.ITEMSTACK;
    }
}
