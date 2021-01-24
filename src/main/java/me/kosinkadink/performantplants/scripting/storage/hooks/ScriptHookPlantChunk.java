package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public abstract class ScriptHookPlantChunk extends ScriptHook {

    protected ScriptBlock currentBlock = ScriptResult.TRUE;

    public ScriptHookPlantChunk(HookAction action) {
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

    protected ChunkLocation createChunkLocation(Player player, PlantBlock plantBlock) {
        ChunkLocation chunkLocation = null;
        if (getCurrentBlockValue(player, plantBlock)) {
            chunkLocation = new ChunkLocation(plantBlock);
        }
        return chunkLocation;
    }

}
