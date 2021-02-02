package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationHasMainHand extends ScriptOperationInventory {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            ItemStack itemStack = context.getPlayer().getInventory().getItemInMainHand();
            return new ScriptResult(!itemStack.getType().isAir());
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
