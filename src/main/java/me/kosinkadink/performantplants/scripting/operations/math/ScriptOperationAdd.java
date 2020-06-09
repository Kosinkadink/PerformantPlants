package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.entity.Player;

public class ScriptOperationAdd extends ScriptOperationBinaryMath {

    public ScriptOperationAdd(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        // if left or right is variable, use proper temp value;
        ScriptResult leftInstance = getLeft().loadValue(plantBlock, player);
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        if (leftInstance.getType() == ScriptType.STRING) {
            return new ScriptResult(leftInstance.getStringValue() + rightInstance.getStringValue());
        }
        if (leftInstance.getType() != ScriptType.DOUBLE && rightInstance.getType() != ScriptType.DOUBLE) {
            return new ScriptResult(leftInstance.getLongValue() + rightInstance.getLongValue());
        }
        return new ScriptResult(leftInstance.getDoubleValue() + rightInstance.getDoubleValue());
    }

    @Override
    protected void validateInputs() {
        ScriptBlock left = getLeft();
        ScriptBlock right = getRight();
        if (left == null || right == null ||
                !(left.getType() == ScriptType.LONG || left.getType() == ScriptType.DOUBLE || left.getType() == ScriptType.BOOLEAN || left.getType() == ScriptType.STRING) ||
                !(right.getType() == ScriptType.LONG || right.getType() == ScriptType.DOUBLE || right.getType() == ScriptType.BOOLEAN || left.getType() == ScriptType.STRING)) {
            throw new IllegalArgumentException("Add operation only supports ScriptType LONG, DOUBLE, BOOLEAN, and STRING");
        }
        if (left.getType() != ScriptType.STRING && right.getType() == ScriptType.STRING) {
            throw new IllegalArgumentException("Add operation does not allow ScriptType STRING to be added to non-STRING type");
        }
    }

    @Override
    protected void setType() {
        if (getLeft().getType() == ScriptType.STRING) {
            type = ScriptType.STRING;
            return;
        }
        super.setType();
    }

}
