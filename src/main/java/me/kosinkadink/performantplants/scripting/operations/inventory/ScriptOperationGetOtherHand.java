package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;

public class ScriptOperationGetOtherHand extends ScriptOperationInventory {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet() && context.isEquipmentSlotSet()) {
            if (context.getEquipmentSlot() == EquipmentSlot.HAND) {
                return new ScriptResult(context.getPlayer().getInventory().getItemInOffHand());
            }
            else if (context.getEquipmentSlot() == EquipmentSlot.OFF_HAND) {
                return new ScriptResult(context.getPlayer().getInventory().getItemInMainHand());
            }
        }
        return ScriptResult.AIR;
    }

    @Override
    protected void setType() {
        type = ScriptType.ITEMSTACK;
    }
}
