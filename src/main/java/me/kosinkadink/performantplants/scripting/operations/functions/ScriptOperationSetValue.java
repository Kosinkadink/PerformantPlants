package me.kosinkadink.performantplants.scripting.operations.functions;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
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
        if (leftInstance.isVariable() && plantBlock != null && plantBlock.getPlantData() != null) {
            PlantData plantData = plantBlock.getPlantData();
            switch (leftInstance.getType()) {
                case STRING:
                    plantData.getData().put(leftInstance.getVariableName(), rightInstance.getStringValue());
                    break;
                case LONG:
                    plantData.getData().put(leftInstance.getVariableName(), rightInstance.getLongValue());
                    break;
                case DOUBLE:
                    plantData.getData().put(leftInstance.getVariableName(), rightInstance.getDoubleValue());
                    break;
                case BOOLEAN:
                    plantData.getData().put(leftInstance.getVariableName(), rightInstance.getBooleanValue());
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

}
