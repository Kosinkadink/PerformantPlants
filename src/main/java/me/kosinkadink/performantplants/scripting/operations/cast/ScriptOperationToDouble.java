package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationToDouble extends ScriptOperationCast {

    public ScriptOperationToDouble(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        return new ScriptResult(getInput().loadValue(context).getDoubleValue());
    }

}
