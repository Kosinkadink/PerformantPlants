package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationXor extends ScriptOperationBinaryLogic {

    public ScriptOperationXor(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }


    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        // if left or right is variable, use proper temp value;
        boolean leftInstance = getLeft().loadValue(plantBlock, player).getBooleanValue();
        boolean rightInstance = getRight().loadValue(plantBlock, player).getBooleanValue();
        // only true if not the same values
        return new ScriptResult(leftInstance ^ rightInstance);
    }
}
