package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationOr extends ScriptOperationBinaryLogic {

    public ScriptOperationOr(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        // load values with short circuiting
        return new ScriptResult(
                getLeft().loadValue(plantBlock, player).getBooleanValue() ||
                        getRight().loadValue(plantBlock, player).getBooleanValue()
        );
    }

}
