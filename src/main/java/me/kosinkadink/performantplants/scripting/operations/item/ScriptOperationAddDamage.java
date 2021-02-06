package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationAddDamage extends ScriptOperationItemInputs {

    public ScriptOperationAddDamage(ScriptBlock itemStack, ScriptBlock amount) {
        super(itemStack, amount);
    }

    public ScriptBlock getItemStack() {
        return inputs[0];
    }

    public ScriptBlock getAmount() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ItemStack itemStack = getItemStack().loadValue(context).getItemStackValue();
        int amount = getAmount().loadValue(context).getIntegerValue();
        return new ScriptResult(ItemHelper.updateDamage(itemStack, amount));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isItemStack(getItemStack())) {
            throw new IllegalArgumentException("Requires ScriptType ITEMSTACK for itemstack");
        }
        if (!ScriptHelper.isLong(getAmount())) {
            throw new IllegalArgumentException("Requires ScriptType LONG for amount");
        }
    }
}
