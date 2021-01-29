package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.ScriptHelper;

public class ScriptOperationSetValueScopeParameter extends ScriptOperation {

    public ScriptOperationSetValueScopeParameter(ScriptBlock plantId, ScriptBlock scope, ScriptBlock parameter, ScriptBlock variableName, ScriptBlock value) {
        super(plantId, scope, parameter, variableName, value);
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

    public ScriptBlock getValue() {
        return inputs[4];
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        String plantId = getPlantId().loadValue(context).getStringValue();
        String scope = getScope().loadValue(context).getStringValue();
        String parameter = getParameter().loadValue(context).getStringValue();
        String variableName = getVariableName().loadValue(context).getStringValue();
        Object value = getValue().loadValue(context).getValue();
        return new ScriptResult(PerformantPlants.getInstance().getPlantTypeManager().updateVariable(plantId, scope, parameter, variableName, value));
    }

    @Override
    protected void setType() {
        type = getValue().getType();
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
    public ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }
}
