package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationFunction extends ScriptOperation {

    public ScriptOperationFunction(ScriptBlock ... inputs) {
        super(inputs);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult result = null;
        for (ScriptBlock input : inputs) {
            result = input.loadValue(context);
        }
        return result;
    }

    @Override
    protected void setType() {
        // set type to last item
        type = inputs[inputs.length-1].getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Function requires at least one input");
        }
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FLOW;
    }

}
