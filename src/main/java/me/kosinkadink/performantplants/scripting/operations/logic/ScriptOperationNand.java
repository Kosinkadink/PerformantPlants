package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

import javax.annotation.Nonnull;

public class ScriptOperationNand extends ScriptOperationAnd {

    public ScriptOperationNand(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        // opposite of 'and' result
        return new ScriptResult(!super.perform(context).getBooleanValue());
    }

}
