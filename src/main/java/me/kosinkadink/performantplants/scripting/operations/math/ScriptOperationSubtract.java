package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

public class ScriptOperationSubtract extends ScriptOperationBinaryMath {

    public ScriptOperationSubtract(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) {
        // if left or right is variable, use proper temp value;
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
        if (leftInstance.getType() != ScriptType.DOUBLE && rightInstance.getType() != ScriptType.DOUBLE) {
            return new ScriptResult(leftInstance.getLongValue() - rightInstance.getLongValue());
        }
        return new ScriptResult(leftInstance.getDoubleValue() - rightInstance.getDoubleValue());
    }

}
