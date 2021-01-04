package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.entity.Player;

public class ScriptOperationEffects extends ScriptOperation {

    private final PlantEffectStorage storage;

    public ScriptOperationEffects(PlantEffectStorage storage) {
        this.storage = storage;
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        if (storage != null) {
            if (player == null) {
                storage.performEffects(plantBlock.getBlock(), plantBlock);
            } else {
                storage.performEffects(player, plantBlock);
            }
        }
        return ScriptResult.TRUE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }
}
