package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.EnumHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.block.BlockFace;

import javax.annotation.Nonnull;

public class ScriptOperationIsBlockFace extends ScriptOperationBlock {

    public ScriptOperationIsBlockFace(ScriptBlock blockFace) {
        super(blockFace);
    }

    public ScriptBlock getBlockFace() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isBlockFaceSet()) {
            BlockFace blockFace = EnumHelper.getBlockFace(getBlockFace().loadValue(context).getStringValue());
            if (blockFace != null) {
                if (context.isPlantBlockSet() && context.getPlantBlock().getPlant().isRotatePlant()) {
                    blockFace = BlockHelper.getNormalizedBlockFaceDirection(blockFace, context.getEffectivePlantBlock().getDirection().getOppositeFace());
                }
                return new ScriptResult(blockFace == context.getBlockFace());
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isString(getBlockFace())) {
            throw new IllegalArgumentException(String.format("input must be ScriptType STRING, not %s", getBlockFace().getType()));
        }
    }
}
