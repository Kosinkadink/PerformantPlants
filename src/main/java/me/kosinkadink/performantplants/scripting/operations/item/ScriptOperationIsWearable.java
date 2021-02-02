package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ItemHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationIsWearable extends ScriptOperationItemInputs {

    public ScriptOperationIsWearable(ScriptBlock itemStack) {
        super(itemStack);
    }

    public ScriptBlock getItemStack() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ItemStack itemStack = getItemStack().loadValue(context).getItemStackValue();
        return new ScriptResult(ItemHelper.isMaterialWearable(itemStack));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
