package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.block.BlockFace;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class ScriptOperationRequiredBlockFaces extends ScriptOperationBlock {

    private final HashSet<BlockFace> blockFaces;

    public ScriptOperationRequiredBlockFaces(HashSet<BlockFace> blockFaces) {
        this.blockFaces = blockFaces;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isBlockFaceSet()) {
            BlockFace blockFace = context.getBlockFace();
            if (context.isPlantBlockSet() && context.getPlantBlock().getPlant().isRotatePlant()) {
                blockFace = BlockHelper.getNormalizedBlockFaceDirectionSubtract(
                        blockFace, context.getEffectivePlantBlock().getDirection().getOppositeFace());
            }
            if (blockFaces.contains(blockFace)) {
                return ScriptResult.TRUE;
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
