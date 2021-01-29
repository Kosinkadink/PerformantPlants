package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.*;

public class ScriptOperationPassOnlyBlock extends ScriptOperation {

    public ScriptOperationPassOnlyBlock(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        return getScriptBlock().loadValue(new ExecutionContext().set(context.getPlantBlock()));
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getScriptBlock() == null) {
            throw new IllegalArgumentException("ScriptBlock cannot be null for ScriptOperationPassOnlyBlock");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.BLOCK;
    }
}
