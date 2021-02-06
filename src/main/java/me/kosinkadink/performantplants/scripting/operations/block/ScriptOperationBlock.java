package me.kosinkadink.performantplants.scripting.operations.block;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationBlock extends ScriptOperationNoOptimize {

    public ScriptOperationBlock(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.BLOCK;
    }

}
