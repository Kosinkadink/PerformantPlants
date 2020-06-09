package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        if (storage != null) {
            EquipmentSlot hand = getHand(getUseMainHand().loadValue(plantBlock, player).getBooleanValue());
            ItemStack itemStack;
            if (player != null) {
                itemStack = player.getInventory().getItem(hand);
            } else {
                itemStack = new ItemStack(Material.AIR);
            }
            PlantInteract interact = storage.getPlantInteract(itemStack);
            if (interact != null) {
                interact.getEffectStorage().performEffects(plantBlock.getBlock(), plantBlock);
                if (player != null && interact.getConsumableStorage() != null) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(player, hand);
                    consumable.getEffectStorage().performEffects(player, plantBlock);
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
