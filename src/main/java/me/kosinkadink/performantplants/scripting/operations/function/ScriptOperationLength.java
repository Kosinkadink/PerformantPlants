package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;
import org.bukkit.entity.Player;

public class ScriptOperationLength extends ScriptOperationUnary {


    public ScriptOperationLength(ScriptBlock input) {
        super(input);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        return new ScriptResult(getInput().loadValue(plantBlock, player).getStringValue().length());
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    protected void validateInputs() {
        if (getInput().getType() != ScriptType.STRING) {
            throw new IllegalArgumentException("Length operation on supports ScriptType STRING");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

}
