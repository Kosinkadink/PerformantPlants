package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

public class ScriptOperationGreaterThan extends ScriptOperationCompare {

    public ScriptOperationGreaterThan(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    protected void validateInputs() {
        super.validateInputs();
        if (getLeft().getType() == ScriptType.BOOLEAN || getRight().getType() == ScriptType.BOOLEAN) {
            throw new IllegalArgumentException("GreaterThan operation does not support ScriptType BOOLEAN");
        }
    }

    @Override
    public ScriptResult perform(ExecutionContext context) {
        // if left or right is variable, use proper temp value;
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
        // check if types compatible
        if (leftInstance.getType() != leftInstance.getType()) {
            if (leftInstance.getType() != ScriptType.LONG || leftInstance.getType() != ScriptType.DOUBLE ||
                    rightInstance.getType() != ScriptType.LONG || rightInstance.getType() != ScriptType.DOUBLE) {
                return new ScriptResult(Boolean.FALSE);
            }
        }
        switch (leftInstance.getType()) {
            case LONG:
            case DOUBLE:
                return new ScriptResult(leftInstance.getDoubleValue() > rightInstance.getDoubleValue());
            case STRING:
                return new ScriptResult(leftInstance.getStringValue().compareTo(rightInstance.getStringValue()) > 0);
            default:
                return new ScriptResult(Boolean.FALSE);
        }
    }
}
