package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

import javax.annotation.Nonnull;

public class ScriptOperationNor extends ScriptOperationOr {

    public ScriptOperationNor(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        // opposite of 'or' result
        return new ScriptResult(!super.perform(context).getBooleanValue());
    }

}
