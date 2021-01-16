package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.entity.Player;

public class ScriptOperationIsPlayerSprinting extends ScriptOperation {

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        // also check for sneaking, as player.isSprinting() would be true if player starts sneaking while sprinting
        return new ScriptResult(player != null && player.isSprinting() && !player.isSneaking());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }

}
