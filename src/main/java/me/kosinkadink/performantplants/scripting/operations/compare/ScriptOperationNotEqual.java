package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

import javax.annotation.Nonnull;

public class ScriptOperationNotEqual extends ScriptOperationEqual {

    public ScriptOperationNotEqual(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        ScriptResult result = super.perform(context);
        if (result == null) {
            return null;
        }
        return result.getBooleanValue() ? ScriptResult.FALSE : ScriptResult.TRUE;
    }

}
