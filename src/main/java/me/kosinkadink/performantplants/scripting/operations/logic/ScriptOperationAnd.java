package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

import javax.annotation.Nonnull;

public class ScriptOperationAnd extends ScriptOperationBinaryLogic {

    public ScriptOperationAnd(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        // load values with short circuiting
        return new ScriptResult(
                getLeft().loadValue(context).getBooleanValue() &&
                        getRight().loadValue(context).getBooleanValue()
        );
    }

}
