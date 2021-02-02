package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationSetValue extends ScriptOperation {

    public ScriptOperationSetValue(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    public ScriptBlock getLeft() {
        return inputs[0];
    }

    public ScriptBlock getRight() {
        return inputs[1];
    }

    @Override
    protected void validateInputs() {
        if (!(getLeft() instanceof ScriptResult) || !getLeft().containsVariable()) {
            throw new IllegalArgumentException("Left argument must be variable and not an operation");
        }
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        ScriptResult leftInstance = (ScriptResult) getLeft();
        ScriptResult rightInstance = getRight().loadValue(context);
        if (leftInstance.isVariable()) {
            String variableName = PlaceholderHelper.setVariablesAndPlaceholders(context, leftInstance.getVariableName());
            switch (leftInstance.getType()) {
                case STRING:
                    ScriptHelper.updateAnyDataVariableValue(context, variableName, rightInstance.getStringValue());
                    break;
                case LONG:
                    ScriptHelper.updateAnyDataVariableValue(context, variableName, rightInstance.getLongValue());
                    break;
                case DOUBLE:
                    ScriptHelper.updateAnyDataVariableValue(context, variableName, rightInstance.getDoubleValue());
                    break;
                case BOOLEAN:
                    ScriptHelper.updateAnyDataVariableValue(context, variableName, rightInstance.getBooleanValue());
                    break;
                case ITEMSTACK:
                    ScriptHelper.updateAnyDataVariableValue(context, variableName, rightInstance.getItemStackValue());
                    break;
                default:
                    break;
            }
        }
        return rightInstance;
    }

    @Override
    protected void setType() {
        type = getLeft().getType();
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

}
