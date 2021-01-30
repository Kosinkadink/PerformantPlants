package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.scripting.*;

import javax.annotation.Nonnull;

public class ScriptOperationWrapData extends ScriptOperation {

    protected final ScriptPlantData scriptPlantData;

    public ScriptOperationWrapData(@Nonnull ScriptPlantData scriptPlantData, @Nonnull ScriptBlock scriptBlock) {
        super(scriptBlock);
        this.scriptPlantData = scriptPlantData;
    }

    public ScriptBlock getScriptBlock() {
        return inputs[0];
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        // wrap context
        ExecutionWrapper wrapper = new ExecutionWrapper(scriptPlantData.createPlantData(context));
        wrapper.wrap(context);
        // load value with wrapped context
        ScriptResult result = getScriptBlock().loadValue(context);
        // unwrap context
        wrapper.unwrap(context);
        return result;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    protected void setType() {
        type = getScriptBlock().getType();
    }
}
