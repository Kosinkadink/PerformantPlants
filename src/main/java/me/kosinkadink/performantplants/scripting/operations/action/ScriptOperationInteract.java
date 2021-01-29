package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ScriptOperationInteract extends ScriptOperation {

    private final PlantInteractStorage storage;

    public ScriptOperationInteract(PlantInteractStorage storage, ScriptBlock useMainHand) {
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
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        if (storage != null) {
            EquipmentSlot hand = getHand(getUseMainHand().loadValue(context).getBooleanValue());
            ItemStack itemStack;
            if (context.isPlayerSet()) {
                itemStack = context.getPlayer().getInventory().getItem(hand);
            } else {
                itemStack = new ItemStack(Material.AIR);
            }
            PlantInteract interact = storage.getPlantInteract(context.copy().set(itemStack));
            if (interact != null) {
                interact.getEffectStorage().performEffectsBlock(context);
                if (context.isPlayerSet() && interact.getConsumableStorage() != null) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(context, hand);
                    consumable.getEffectStorage().performEffectsPlayer(context);
                }
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
