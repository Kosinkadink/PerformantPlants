package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationNotEqual extends ScriptOperationEqual {

    public ScriptOperationNotEqual(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        ScriptResult result = super.perform(plantBlock, player);
        if (result == null) {
            return null;
        }
        return result.getBooleanValue() ? ScriptResult.FALSE : ScriptResult.TRUE;
    }

}
