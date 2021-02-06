package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationDelay extends ScriptOperationNoOptimize {

    public ScriptOperationDelay(ScriptBlock delay, ScriptBlock scriptBlock) {
        super(delay, scriptBlock);
    }

    public ScriptBlock getDelay() {
        return inputs[0];
    }

    public ScriptBlock getScriptBlock() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        long delayAmount = getDelay().loadValue(context).getIntegerValue();
        if (delayAmount > 0) {
            ExecutionContext archivedContext = context.copy();
            PerformantPlants.getInstance().getServer().getScheduler().runTaskLater(
                    PerformantPlants.getInstance(),
                    () -> getScriptBlock().loadValue(archivedContext),
                    delayAmount
            );
        } else {
            getScriptBlock().loadValue(context);
        }
        return ScriptResult.TRUE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isLong(getDelay())) {
            throw new IllegalArgumentException(String.format("delay must be ScriptType LONG, not %s", getDelay().getType()));
        }
    }

}
