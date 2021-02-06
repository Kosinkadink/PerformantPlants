package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.Location;

import javax.annotation.Nonnull;

public class ScriptOperationExplosion extends ScriptOperationAction {

    public ScriptOperationExplosion(ScriptBlock power, ScriptBlock fire, ScriptBlock breakBlocks) {
        super(power, fire, breakBlocks);
    }

    public ScriptBlock getPower() {
        return inputs[0];
    }

    public ScriptBlock getFire() {
        return inputs[1];
    }

    public ScriptBlock getBreakBlocks() {
        return inputs[2];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isLocationPossible()) {
            Location location = context.getLocation();
            try {
                if (location.getWorld() != null) {
                    return new ScriptResult(location.getWorld().createExplosion(
                            location,
                            getPower().loadValue(context).getFloatValue(),
                            getFire().loadValue(context).getBooleanValue(),
                            getBreakBlocks().loadValue(context).getBooleanValue()
                    ));
                }
            } catch (IllegalArgumentException e) {
                // do nothing, just prevent runtime exceptions
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
