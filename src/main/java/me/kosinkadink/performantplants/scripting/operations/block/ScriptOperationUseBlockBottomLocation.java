package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Location;

import javax.annotation.Nonnull;

public class ScriptOperationUseBlockBottomLocation extends ScriptOperationBlock {

    public ScriptOperationUseBlockBottomLocation(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        Location originalLocation = context.getLocation();
        if (context.isPlantBlockSet()) {
            context.setLocation(BlockHelper.getCenter(context.getPlantBlock().getBlock()).subtract(0, 0.5, 0));
        }
        ScriptResult result = getScriptBlock().loadValue(context);
        // set location back to original
        context.setLocation(originalLocation);
        return result;
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getScriptBlock() == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
    }
}
