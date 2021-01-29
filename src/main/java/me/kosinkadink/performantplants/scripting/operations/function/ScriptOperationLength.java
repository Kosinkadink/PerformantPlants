package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;

import javax.annotation.Nonnull;

public class ScriptOperationLength extends ScriptOperationUnary {


    public ScriptOperationLength(ScriptBlock input) {
        super(input);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(getInput().loadValue(context).getStringValue().length());
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    protected void validateInputs() {
        if (getInput().getType() != ScriptType.STRING) {
            throw new IllegalArgumentException("Length operation on supports ScriptType STRING");
        }
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

}
