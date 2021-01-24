package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public abstract class ScriptHookPlantBlock extends ScriptHook {

    protected ScriptBlock currentBlock = ScriptResult.TRUE;

    public ScriptHookPlantBlock(HookAction action) {
        super(action);
    }

    public ScriptBlock getCurrentBlock() {
        return currentBlock;
    }

    public boolean getCurrentBlockValue(Player player, PlantBlock plantBlock) {
        return currentBlock.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setCurrentBlock(ScriptBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    protected BlockLocation createBlockLocation(Player player, PlantBlock plantBlock) {
        BlockLocation blockLocation = null;
        if (getCurrentBlockValue(player, plantBlock)) {
            blockLocation = plantBlock.getLocation();
        }
        return blockLocation;
    }

}
