package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationIsPlayerNull extends ScriptOperationPlayer {

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(!context.isPlayerSet());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
