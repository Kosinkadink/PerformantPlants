package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationNand extends ScriptOperationAnd {

    public ScriptOperationNand(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        // opposite of 'and' result
        return new ScriptResult(!super.perform(plantBlock, player).getBooleanValue());
    }

}
