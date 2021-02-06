package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationIsPlayerDead extends ScriptOperationPlayer {

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(context.isPlayerSet() && context.getPlayer().isDead());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

}
