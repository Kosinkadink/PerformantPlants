package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationGetPlayerDisplayName extends ScriptOperationPlayer {
    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            return new ScriptResult(context.getPlayer().getDisplayName());
        }
        return ScriptResult.EMPTY;
    }

    @Override
    protected void setType() {
        type = ScriptType.STRING;
    }
}
