package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public abstract class ScriptHookPlantBlock extends ScriptHook {

    protected ScriptBlock currentBlock = ScriptResult.TRUE;

    public ScriptHookPlantBlock(HookAction action) {
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

    protected BlockLocation createBlockLocation(ExecutionContext context) {
        BlockLocation blockLocation = null;
        if (getCurrentBlockValue(context)) {
            blockLocation = context.getPlantBlock().getLocation();
        }
        return blockLocation;
    }

}
