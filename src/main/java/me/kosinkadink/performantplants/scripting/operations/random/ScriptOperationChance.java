package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;
import me.kosinkadink.performantplants.util.RandomHelper;

public class ScriptOperationChance extends ScriptOperationUnary {


    public ScriptOperationChance(ScriptBlock input) {
        super(input);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(RandomHelper.generateChancePercentage(
                getInput().loadValue(context).getDoubleValue())
        );
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getInput().getType() != ScriptType.LONG && getInput().getType() != ScriptType.DOUBLE) {
            throw new IllegalArgumentException("Chance operation only supports ScriptType LONG and DOUBLE");
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.RANDOM;
    }

}
