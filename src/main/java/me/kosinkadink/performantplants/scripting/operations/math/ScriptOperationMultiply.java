package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinaryMath;
import org.bukkit.entity.Player;

public class ScriptOperationMultiply extends ScriptOperationBinaryMath {

    public ScriptOperationMultiply(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        // if left or right is variable, use proper temp value;
        ScriptResult leftInstance = getLeft().loadValue(plantBlock, player);
        ScriptResult rightInstance = getRight().loadValue(plantBlock, player);
        if (leftInstance.getType() != ScriptType.DOUBLE && rightInstance.getType() != ScriptType.DOUBLE) {
            return new ScriptResult(leftInstance.getLongValue() * rightInstance.getLongValue());
        }
        return new ScriptResult(leftInstance.getDoubleValue() * rightInstance.getDoubleValue());
    }

}
