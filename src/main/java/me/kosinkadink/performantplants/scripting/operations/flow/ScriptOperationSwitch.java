package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ScriptOperationSwitch extends ScriptOperation {

    final int casesLength;
    HashMap<ScriptResult, ScriptBlock> caseMap = new HashMap<>();

    public ScriptOperationSwitch(ScriptBlock[] scriptBlocks, ScriptResult[] cases) {
        super(scriptBlocks);
        casesLength = cases.length;
        for (int i = 0; i < cases.length; i++) {
            caseMap.put(cases[i], inputs[i+1]);
        }
    }

    public ScriptBlock getCondition() {
        return inputs[0];
    }

    public ScriptBlock getDefault() {
        if (casesLength+2 == inputs.length) {
            return inputs[inputs.length - 1];
        }
        return null;
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        ScriptResult condition = getCondition().loadValue(plantBlock, player);
        ScriptBlock action = caseMap.get(condition);
        if (action != null) {
            return action.loadValue(plantBlock, player);
        }
        action = getDefault();
        if (action != null) {
            return action.loadValue(plantBlock, player);
        }
        return ScriptResult.getDefaultOfType(getType());
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.FLOW;
    }

    @Override
    protected void setType() {
        type = getCondition().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length < 1) {
            throw new IllegalArgumentException("Not enough inputs for create ScriptOperationSwitch");
        }
    }
}
