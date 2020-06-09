package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationFunction extends ScriptOperation {

    public ScriptOperationFunction(ScriptBlock ... inputs) {
        super(inputs);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        ScriptResult result = null;
        for (ScriptBlock input : inputs) {
            result = input.loadValue(plantBlock, player);
        }
        return result;
    }

    @Override
    protected void setType() {
        // set type to last item
        type = inputs[inputs.length-1].getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Function requires at least one input");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.FLOW;
    }

}
