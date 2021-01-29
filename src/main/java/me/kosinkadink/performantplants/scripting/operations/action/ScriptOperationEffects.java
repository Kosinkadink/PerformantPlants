package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;

public class ScriptOperationEffects extends ScriptOperation {

    private final PlantEffectStorage storage;

    public ScriptOperationEffects(PlantEffectStorage storage) {
        this.storage = storage;
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        if (storage != null) {
            storage.performEffectsDynamic(context);
        }
        return ScriptResult.TRUE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }
}
