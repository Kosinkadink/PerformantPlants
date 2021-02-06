package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationDoIf extends ScriptOperationFlow {

    public ScriptOperationDoIf(ScriptBlock condition, ScriptBlock scriptBlock) {
        super(condition, scriptBlock);
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    public ScriptBlock getScriptBlock() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult conditionInstance = getCondition().loadValue(context);
        if (conditionInstance.getBooleanValue()) {
            getScriptBlock().loadValue(context);
            return ScriptResult.TRUE;
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (ScriptHelper.isBoolean(getCondition())) {
            throw new IllegalArgumentException("condition should be ScriptType BOOLEAN, not " + getCondition().getType());
        }
    }
}
