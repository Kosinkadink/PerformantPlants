package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.DestroyReason;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationBreakBlock extends ScriptOperationBlock {

    public ScriptOperationBreakBlock(ScriptBlock drops) {
        super(drops);
    }

    public ScriptBlock getDrops() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlantBlockSet() && (!context.isDestroyReasonSet() || !context.getDestroyReason().isRelative())) {
            DestroyReason destroyReason = DestroyReason.BREAK;
            if (context.isDestroyReasonSet()) {
                destroyReason = DestroyReason.RELATIVE_BREAK;
            }
            return new ScriptResult(BlockHelper.destroyPlantBlock(PerformantPlants.getInstance(), context.getPlantBlock(), destroyReason, context));
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isBoolean(getDrops())) {
            throw new IllegalArgumentException("Requires ScriptType BOOLEAN for input");
        }
    }
}
