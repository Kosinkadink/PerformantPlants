package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.inventory.ItemStack;

public class RequiredItem {

    private ItemStack itemStack;

    private ScriptBlock condition = ScriptResult.TRUE;

    private ScriptBlock takeItem = ScriptResult.FALSE;
    private ScriptBlock inHand = ScriptResult.FALSE;
    private ScriptBlock addDamage = ScriptResult.ZERO;

    public RequiredItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    // item stack
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    // condition
    public boolean isConditionMet(ExecutionContext context) {
        return condition.loadValue(context).getBooleanValue();
    }

    public void setCondition(ScriptBlock condition) {
        this.condition = condition;
    }

    // take item
    public boolean isTakeItem(ExecutionContext context) {
        return takeItem.loadValue(context).getBooleanValue();
    }

    public void setTakeItem(ScriptBlock takeItem) {
        this.takeItem = takeItem;
    }

    // in hand
    public boolean isInHand(ExecutionContext context) {
        return inHand.loadValue(context).getBooleanValue();
    }

    public void setInHand(ScriptBlock inHand) {
        this.inHand = inHand;
    }

    // add damage
    public int getAddDamage(ExecutionContext context) {
        return addDamage.loadValue(context).getIntegerValue();
    }

    public void setAddDamage(ScriptBlock addDamage) {
        this.addDamage = addDamage;
    }
}
