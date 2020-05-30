package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;
import me.kosinkadink.performantplants.util.RandomHelper;
import org.bukkit.entity.Player;

public class ScriptOperationChance extends ScriptOperationUnary {


    public ScriptOperationChance(ScriptBlock input) {
        super(input);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        return new ScriptResult(RandomHelper.generateChancePercentage(
                getInput().loadValue(plantBlock, player).getDoubleValue())
        );
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getInput().getType() != ScriptType.LONG || getInput().getType() != ScriptType.DOUBLE) {
            throw new IllegalArgumentException("Chance operation only supports ScriptType LONG and DOUBLE");
        }
    }
}
