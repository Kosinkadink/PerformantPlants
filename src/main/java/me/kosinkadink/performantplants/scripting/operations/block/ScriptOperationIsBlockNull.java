package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationIsBlockNull extends ScriptOperationBlock {

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        return new ScriptResult(!context.isPlantBlockSet());
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
