package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.function.ScriptOperationSetValue;
import org.bukkit.entity.Player;

public class ScriptOperationSubtractFrom extends ScriptOperationSubtract {

    public ScriptOperationSubtractFrom(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) {
        ScriptResult result = super.perform(plantBlock, player);
        // if left was a variable, set the result to its new value
        if (getLeft() instanceof ScriptResult && getLeft().containsVariable()) {
            // perform set value
            new ScriptOperationSetValue(getLeft(), result).perform(plantBlock);
        }
        return result;
    }

    @Override
    protected void validateInputs() {
        super.validateInputs();
        if (getLeft().getType() == ScriptType.BOOLEAN) {
            throw new IllegalArgumentException("Left argument cannot be ScriptType BOOLEAN for SubtractFrom operation");
        }
    }

    @Override
    protected void setType() {
        super.setType();
        if (getLeft() instanceof ScriptResult && getLeft().containsVariable()) {
            type = getLeft().getType();
        }
    }

}
