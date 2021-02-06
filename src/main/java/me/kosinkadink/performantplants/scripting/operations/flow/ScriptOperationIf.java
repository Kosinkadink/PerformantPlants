package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationIf extends ScriptOperationFlow {

    public ScriptOperationIf(ScriptBlock condition, ScriptBlock then) {
        this(condition, then, ScriptResult.getDefaultOfType(then.getType()));
    }

    public ScriptOperationIf(ScriptBlock condition, ScriptBlock then, ScriptBlock elseBlock) {
        super(condition, then, elseBlock);
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    public ScriptBlock getThen() {
        return inputs[1];
    }

    public ScriptBlock getElse() {
        return inputs[2];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult conditionInstance = getCondition().loadValue(context);
        if (conditionInstance.getBooleanValue()) {
            return getThen().loadValue(context);
        } else {
            return getElse().loadValue(context);
        }
    }

    @Override
    protected void setType() {
        type = getThen().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        // then and else must match types, or illegal argument exception
        if (getThen().getType() != getElse().getType()) {
            throw new IllegalArgumentException("then and else blocks did not have matching ScriptType");
        }
    }

}
