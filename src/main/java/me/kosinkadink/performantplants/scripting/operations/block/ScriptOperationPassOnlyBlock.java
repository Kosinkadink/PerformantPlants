package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationPassOnlyBlock extends ScriptOperationBlock {

    public ScriptOperationPassOnlyBlock(ScriptBlock scriptBlock) {
        super(scriptBlock);
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        return getScriptBlock().loadValue(new ExecutionContext().set(context.getPlantBlock()));
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (getScriptBlock() == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
    }

}
