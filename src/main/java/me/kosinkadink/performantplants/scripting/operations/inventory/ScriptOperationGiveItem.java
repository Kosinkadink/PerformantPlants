package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationGiveItem extends ScriptOperationInventory {

    public ScriptOperationGiveItem(ScriptBlock itemStack) {
        super(itemStack);
    }

    public ScriptBlock getItemStack() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            ItemStack itemStack = getItemStack().loadValue(context).getItemStackValue();
            DropHelper.givePlayerItemStack(context.getPlayer(), itemStack);
            return ScriptResult.TRUE;
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {
        if (!ScriptHelper.isItemStack(getItemStack())) {
            throw new IllegalArgumentException("input must be ScriptType ITEMSTACK");
        }
    }
}
