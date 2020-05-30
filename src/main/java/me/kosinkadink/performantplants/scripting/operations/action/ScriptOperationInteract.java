package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ScriptOperationInteract extends ScriptOperation {

    private final PlantInteractStorage storage;
    private final boolean useMainHand;
    private EquipmentSlot hand;

    public ScriptOperationInteract(PlantInteractStorage storage, boolean useMainhand) {
        super();
        this.storage = storage;
        this.useMainHand = useMainhand;
        setHand();

    }

    private void setHand() {
        if (useMainHand) {
            hand = EquipmentSlot.HAND;
        } else {
            hand = EquipmentSlot.OFF_HAND;
        }
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        if (storage != null) {
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
    public boolean containsVariable() {
        return true;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }
}
