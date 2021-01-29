package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationToBoolean extends ScriptOperationCast {

    public ScriptOperationToBoolean(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        return new ScriptResult(getInput().loadValue(context).getBooleanValue());
    }

}
