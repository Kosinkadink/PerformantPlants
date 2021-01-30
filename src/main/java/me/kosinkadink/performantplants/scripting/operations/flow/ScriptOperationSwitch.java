package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ScriptOperationSwitch extends ScriptOperation {

    final int casesLength;
    HashMap<ScriptResult, ScriptBlock> caseMap = new HashMap<>();

    public ScriptOperationSwitch(ScriptBlock[] scriptBlocks, ScriptResult[] cases) {
        super(scriptBlocks);
        casesLength = cases.length;
        for (int i = 0; i < cases.length; i++) {
            caseMap.put(cases[i], inputs[i+1]);
        }
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    public ScriptBlock getDefault() {
        if (casesLength+2 == inputs.length) {
            return inputs[inputs.length - 1];
        }
        return null;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult condition = getCondition().loadValue(context);
        ScriptBlock action = caseMap.get(condition);
        if (action != null) {
            return action.loadValue(context);
        }
        action = getDefault();
        if (action != null) {
            return action.loadValue(context);
        }
        return ScriptResult.getDefaultOfType(getType());
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FLOW;
    }

    @Override
    protected void setType() {
        type = getCondition().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length < 1) {
            throw new IllegalArgumentException("Not enough inputs to create ScriptOperationSwitch");
        }
    }
}
