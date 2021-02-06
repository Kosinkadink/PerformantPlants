package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Location;

import javax.annotation.Nonnull;

public class ScriptOperationUseEyeLocation extends ScriptOperationPlayer {

    public ScriptOperationUseEyeLocation(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        Location originalLocation = context.getLocation();
        if (context.isPlayerSet()) {
            context.setLocation(context.getPlayer().getEyeLocation());
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
