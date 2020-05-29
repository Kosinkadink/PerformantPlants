package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;
import org.bukkit.entity.Player;

public class ScriptOperationNot extends ScriptOperationUnary {

    public ScriptOperationNot(ScriptBlock input) {
        super(input);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        return new ScriptResult(!getInput().loadValue(plantBlock, player).getBooleanValue());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {

    }

}
