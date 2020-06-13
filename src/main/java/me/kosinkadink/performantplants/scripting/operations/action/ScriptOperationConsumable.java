package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class ScriptOperationConsumable extends ScriptOperation {

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
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        if (storage != null) {
            EquipmentSlot hand = getHand(getUseMainHand().loadValue(plantBlock, player).getBooleanValue());
            PlantConsumable consumable = storage.getConsumable(player, hand);
            if (consumable != null) {
                consumable.getEffectStorage().performEffects(player, plantBlock);
            }
        }
        return ScriptResult.TRUE;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

}
