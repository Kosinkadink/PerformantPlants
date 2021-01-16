package me.kosinkadink.performantplants.scripting.operations.world;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ScriptOperationGetWorld extends ScriptOperation {

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        World world;
        if (player != null) {
            world = player.getLocation().getWorld();
            if (world != null) {
                return new ScriptResult(world.getName());
            }
        }
        else if (plantBlock != null) {
            world = plantBlock.getLocation().getWorld();
            if (world != null) {
                return new ScriptResult(world.getName());
            }
        }
        return ScriptResult.EMPTY;
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
        return ScriptCategory.WORLD;
    }
}
