package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationUntilTrue extends ScriptOperationFlow {

    public ScriptOperationUntilTrue(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        // perform all script blocks until the first that returns TRUE
        for (ScriptBlock input : inputs) {
            boolean isTrue = input.loadValue(context).getBooleanValue();
            if (isTrue) {
                return ScriptResult.TRUE;
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Until-true requires at least one input");
        }
        for (ScriptBlock input : inputs) {
            if (!ScriptHelper.isBoolean(input)) {
                throw new IllegalArgumentException("Until-true only accepts script blocks of ScriptType BOOLEAN");
            }
        }
    }
}
