package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.entity.Player;

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
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        String plantId = getPlantId().loadValue(plantBlock, player).getStringValue();
        String scope = getScope().loadValue(plantBlock, player).getStringValue();
        String parameter = getParameter().loadValue(plantBlock, player).getStringValue();
        String variableName = getVariableName().loadValue(plantBlock, player).getStringValue();
        Object value = getValue().loadValue(plantBlock, player).getValue();
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
