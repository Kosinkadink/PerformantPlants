package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;

public class ScriptOperationIsEitherHand extends ScriptOperationInventory {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isEquipmentSlotSet()) {
            return new ScriptResult(context.getEquipmentSlot() == EquipmentSlot.HAND
                    ||context.getEquipmentSlot() == EquipmentSlot.OFF_HAND);
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
