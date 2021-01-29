package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationGetValueScopeParameter extends ScriptOperation {

    private final ScriptType expectedType;

    public ScriptOperationGetValueScopeParameter(ScriptBlock plantId, ScriptBlock scope, ScriptBlock parameter, ScriptBlock variableName, ScriptType expectedType) {
        super(plantId, scope, parameter, variableName);
        if (!ScriptHelper.isSupportedType(expectedType)) {
            throw new IllegalArgumentException("ExpectedType must be ScriptType BOOLEAN, STRING, LONG, or DOUBLE");
        }
        this.expectedType = expectedType;
        setType();
    }

    public ScriptBlock getPlantId() {
        return inputs[0];
    }

    public ScriptBlock getScope() {
        return inputs[1];
    }

    public ScriptBlock getParameter() {
        return inputs[2];
    }

    public ScriptBlock getVariableName() {
        return inputs[3];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        String plantId = getPlantId().loadValue(context).getStringValue();
        String scope = getScope().loadValue(context).getStringValue();
        String parameter = getParameter().loadValue(context).getStringValue();
        String variableName = getVariableName().loadValue(context).getStringValue();
        Object value = PerformantPlants.getInstance().getPlantTypeManager().getVariable(plantId, scope, parameter, variableName);
        // if got null, return default value of expected type
        if (value == null) {
            return ScriptResult.getDefaultOfType(expectedType);
        }
        return new ScriptResult(value);
    }

    @Override
    protected void setType() {
        type = expectedType;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        // script blocks (except for value) must be type STRING
        if (!ScriptHelper.isString(getPlantId()) || !ScriptHelper.isString(getScope())
                || !ScriptHelper.isString(getParameter()) || !ScriptHelper.isString(getVariableName())) {
            throw new IllegalArgumentException("PlantId, scope, parameter, and variableName must be ScriptType STRING");
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }
}
