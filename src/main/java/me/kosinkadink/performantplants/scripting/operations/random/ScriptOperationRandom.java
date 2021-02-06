package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationRandom extends ScriptOperationNoOptimize {

    public ScriptOperationRandom(ScriptBlock... inputs) {
        super(inputs);
    }

    @Override
    public @Nonnull
    ScriptCategory getCategory() {
        return ScriptCategory.RANDOM;
    }
}
