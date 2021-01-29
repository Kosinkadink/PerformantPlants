package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationToString extends ScriptOperationCast {

    public ScriptOperationToString(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.STRING;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        return new ScriptResult(getInput().loadValue(context).getStringValue());
    }

}
