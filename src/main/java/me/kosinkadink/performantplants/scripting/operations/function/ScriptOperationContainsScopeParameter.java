package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationContainsScopeParameter extends ScriptOperation {

    public ScriptOperationContainsScopeParameter(ScriptBlock plantId, ScriptBlock scope, ScriptBlock parameter) {
        super(plantId, scope, parameter);
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

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        String plantId = getPlantId().loadValue(context).getStringValue();
        String scope = getScope().loadValue(context).getStringValue();
        String parameter = getParameter().loadValue(context).getStringValue();
        return new ScriptResult(PerformantPlants.getInstance().getPlantTypeManager().containsScopeParameter(plantId, scope, parameter));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        // script blocks must be type STRING
        if (!ScriptHelper.isString(getPlantId()) || !ScriptHelper.isString(getScope()) || !ScriptHelper.isString(getParameter())) {
            throw new IllegalArgumentException("PlantId, scope, and parameter must be ScriptType STRING");
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
