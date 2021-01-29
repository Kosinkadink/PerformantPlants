package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;

import javax.annotation.Nonnull;

public class ScriptOperationNot extends ScriptOperationUnary {

    public ScriptOperationNot(ScriptBlock input) {
        super(input);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        return new ScriptResult(!getInput().loadValue(context).getBooleanValue());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {

    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.LOGIC;
    }

}
