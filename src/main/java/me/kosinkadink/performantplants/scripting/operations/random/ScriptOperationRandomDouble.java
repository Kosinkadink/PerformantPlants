package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;
import me.kosinkadink.performantplants.util.RandomHelper;

import javax.annotation.Nonnull;

public class ScriptOperationRandomDouble extends ScriptOperationBinary {

    public ScriptOperationRandomDouble(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
        // perform the random generation
        return new ScriptResult(RandomHelper.generateRandomDoubleInRange(
                leftInstance.getDoubleValue(), rightInstance.getDoubleValue()
        ));
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if ((getLeft().getType() != ScriptType.DOUBLE && getLeft().getType() != ScriptType.LONG) ||
                (getRight().getType() != ScriptType.DOUBLE && getRight().getType() != ScriptType.LONG)) {
            throw new IllegalArgumentException("RandomDouble operation only supports ScriptType DOUBLE and LONG");
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.RANDOM;
    }

}
