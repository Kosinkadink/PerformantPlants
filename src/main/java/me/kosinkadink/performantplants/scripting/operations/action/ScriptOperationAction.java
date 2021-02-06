package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationAction extends ScriptOperationNoOptimize {

    public ScriptOperationAction(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }

}
