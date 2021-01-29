package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class ScriptOperationNor extends ScriptOperationOr {

    public ScriptOperationNor(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) {
        // opposite of 'or' result
        return new ScriptResult(!super.perform(context).getBooleanValue());
    }

}
