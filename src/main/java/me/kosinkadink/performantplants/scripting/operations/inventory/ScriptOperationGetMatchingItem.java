package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationGetMatchingItem extends ScriptOperationInventory {

    public ScriptOperationGetMatchingItem(ScriptBlock condition) {
        super(condition);
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            // save original itemstack
            ItemStack originalItemStack = context.getItemStack();
            // iterate through all stacks in inventory
            ItemStack foundItemStack = new ItemStack(Material.AIR);
            for (ItemStack checked : context.getPlayer().getInventory()) {
                context.setItemStack(checked);
                boolean result = getCondition().loadValue(context).getBooleanValue();
                // if script results in True, return itemstack just checked
                if (result) {
                    foundItemStack = checked;
                    break;
                }
            }
            // set context itemstack back to original
            context.setItemStack(originalItemStack);
            // return found itemstack
            return new ScriptResult(foundItemStack);
        }
        return ScriptResult.AIR;
    }

    @Override
    protected void setType() {
        type = ScriptType.ITEMSTACK;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isBoolean(getCondition())) {
            throw new IllegalArgumentException("condition must be ScriptType BOOLEAN");
        }
    }
}
