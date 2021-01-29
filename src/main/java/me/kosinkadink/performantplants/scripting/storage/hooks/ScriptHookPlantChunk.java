package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public abstract class ScriptHookPlantChunk extends ScriptHook {

    protected ScriptBlock currentBlock = ScriptResult.TRUE;

    public ScriptHookPlantChunk(HookAction action) {
        super(action);
    }

    public ScriptBlock getCurrentBlock() {
        return currentBlock;
    }

    public boolean getCurrentBlockValue(ExecutionContext context) {
        return currentBlock.loadValue(context).getBooleanValue();
    }

    public void setCurrentBlock(ScriptBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    protected ChunkLocation createChunkLocation(ExecutionContext context) {
        ChunkLocation chunkLocation = null;
        if (getCurrentBlockValue(context)) {
            chunkLocation = new ChunkLocation(context.getPlantBlock());
        }
        return chunkLocation;
    }

}
