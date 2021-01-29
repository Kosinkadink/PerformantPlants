package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class ScriptOperationOr extends ScriptOperationBinaryLogic {

    public ScriptOperationOr(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) {
        // load values with short circuiting
        return new ScriptResult(
                getLeft().loadValue(context).getBooleanValue() ||
                        getRight().loadValue(context).getBooleanValue()
        );
    }

}
