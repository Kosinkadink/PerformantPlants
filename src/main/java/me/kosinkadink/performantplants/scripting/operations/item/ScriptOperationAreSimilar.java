package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationAreSimilar extends ScriptOperationItemInputs {

    public ScriptOperationAreSimilar(ScriptBlock itemStack1, ScriptBlock itemStack2) {
        super(itemStack1, itemStack2);
    }

    public ScriptBlock getItemStack1() {
        return inputs[0];
    }

    public ScriptBlock getItemStack2() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ItemStack itemStack1 = getItemStack1().loadValue(context).getItemStackValue();
        ItemStack itemStack2 = getItemStack2().loadValue(context).getItemStackValue();
        return new ScriptResult(itemStack1.isSimilar(itemStack2));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
