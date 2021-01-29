package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;

public class ScriptOperationIsPlayerDead extends ScriptOperation {

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(context.isPlayerSet() && context.getPlayer().isDead());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }

}
