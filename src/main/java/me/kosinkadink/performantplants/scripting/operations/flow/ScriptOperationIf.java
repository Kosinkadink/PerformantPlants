package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class ScriptOperationIf extends ScriptOperation {

    public ScriptOperationIf(ScriptBlock condition, ScriptBlock ifTrue) {
        this(condition, ifTrue, ScriptResult.getDefaultOfType(ifTrue.getType()));
    }

    public ScriptOperationIf(ScriptBlock condition, ScriptBlock ifTrue, ScriptBlock ifFalse) {
        super(condition, ifTrue, ifFalse);
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    public ScriptBlock getIfTrue() {
        return inputs[1];
    }

    public ScriptBlock getIfFalse() {
        return inputs[2];
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        ScriptResult conditionInstance = getCondition().loadValue(plantBlock, player);
        if (conditionInstance.getBooleanValue()) {
            return getIfTrue().loadValue(plantBlock, player);
        } else {
            return getIfFalse().loadValue(plantBlock, player);
        }
    }

    @Override
    protected void setType() {
        type = getIfTrue().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        // then and else must match types, or illegal argument exception
        if (getIfTrue().getType() != getIfFalse().getType()) {
            throw new IllegalArgumentException("IfTrue and IfFalse did not have matching ScriptType");
        }
    }
}
