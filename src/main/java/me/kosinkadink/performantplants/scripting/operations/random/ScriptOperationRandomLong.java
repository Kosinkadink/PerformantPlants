package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;
import me.kosinkadink.performantplants.util.RandomHelper;

public class ScriptOperationRandomLong extends ScriptOperationBinary {

    public ScriptOperationRandomLong(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
        // perform the random generation
        return new ScriptResult(RandomHelper.generateRandomLongInRange(
                leftInstance.getLongValue(), rightInstance.getLongValue()
        ));
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getLeft().getType() != ScriptType.LONG ||
                getRight().getType() != ScriptType.LONG) {
            throw new IllegalArgumentException("RandomLong operation only supports ScriptType LONG");
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
