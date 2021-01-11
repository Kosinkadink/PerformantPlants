package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.entity.Player;

public class ScriptOperationToLong extends ScriptOperationCast {

    public ScriptOperationToLong(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        return new ScriptResult(getInput().loadValue(plantBlock, player).getLongValue());
    }

}
