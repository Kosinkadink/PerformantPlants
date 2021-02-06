package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;

public class ScriptOperationConsumable extends ScriptOperationAction {

    private final PlantConsumableStorage storage;

    public ScriptOperationConsumable(PlantConsumableStorage storage, ScriptBlock useMainHand) {
        super(useMainHand);
        this.storage = storage;
    }

    private ScriptBlock getUseMainHand() {
        return inputs[0];
    }

    private EquipmentSlot getHand(boolean useMainHand) {
        if (useMainHand) {
            return EquipmentSlot.HAND;
        }
        return EquipmentSlot.OFF_HAND;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (storage != null) {
            EquipmentSlot hand = getHand(getUseMainHand().loadValue(context).getBooleanValue());
            PlantConsumable consumable = storage.getConsumable(context, hand);
            if (consumable != null) {
                consumable.getEffectStorage().performEffectsDynamic(context);
            }
        }
        return ScriptResult.TRUE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

}
