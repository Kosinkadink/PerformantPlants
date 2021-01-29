package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

import javax.annotation.Nonnull;

public class ScriptOperationXor extends ScriptOperationBinaryLogic {

    public ScriptOperationXor(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }


    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        // if left or right is variable, use proper temp value;
        boolean leftInstance = getLeft().loadValue(context).getBooleanValue();
        boolean rightInstance = getRight().loadValue(context).getBooleanValue();
        // only true if not the same values
        return new ScriptResult(leftInstance ^ rightInstance);
    }
}
