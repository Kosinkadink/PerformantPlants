package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationItem extends ScriptOperationNoOptimize {

    public ScriptOperationItem(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ITEM;
    }

}
