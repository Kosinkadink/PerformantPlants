package me.kosinkadink.performantplants.scripting.operations.world;

import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.World;

import javax.annotation.Nonnull;

public class ScriptOperationGetWorld extends ScriptOperation {

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        World world;
        if (context.isPlayerSet()) {
            world = context.getPlayer().getLocation().getWorld();
            if (world != null) {
                return new ScriptResult(world.getName());
            }
        }
        else if (context.isPlantBlockSet()) {
            world = context.getPlantBlock().getLocation().getWorld();
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
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.WORLD;
    }
}
