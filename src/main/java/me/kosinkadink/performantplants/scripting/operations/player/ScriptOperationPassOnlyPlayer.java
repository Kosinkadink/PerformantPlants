package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;

public class ScriptOperationPassOnlyPlayer extends ScriptOperation {

    public ScriptOperationPassOnlyPlayer(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        return getScriptBlock().loadValue(context);
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getScriptBlock() == null) {
            throw new IllegalArgumentException("ScriptBlock cannot be null for ScriptOperationPassOnlyPlayer");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }
}
