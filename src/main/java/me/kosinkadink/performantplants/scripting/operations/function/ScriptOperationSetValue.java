package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.entity.Player;

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
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        ScriptResult leftInstance = (ScriptResult) getLeft();
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        if (leftInstance.isVariable()) {
            PlantBlock effectivePlantBlock = null;
            PlantData plantData = null;
            if (plantBlock != null) {
                effectivePlantBlock = plantBlock.getEffectivePlantBlock();
                plantData = effectivePlantBlock.getEffectivePlantData();
            }
            String variableName = PlaceholderHelper.setVariablesAndPlaceholders(effectivePlantBlock, player, leftInstance.getVariableName());
            switch (leftInstance.getType()) {
                case STRING:
                    ScriptHelper.updateGlobalPlantDataVariableValue(plantData, variableName, rightInstance.getStringValue());
                    break;
                case LONG:
                    ScriptHelper.updateGlobalPlantDataVariableValue(plantData, variableName, rightInstance.getLongValue());
                    break;
                case DOUBLE:
                    ScriptHelper.updateGlobalPlantDataVariableValue(plantData, variableName, rightInstance.getDoubleValue());
                    break;
                case BOOLEAN:
                    ScriptHelper.updateGlobalPlantDataVariableValue(plantData, variableName, rightInstance.getBooleanValue());
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
    public ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

}
