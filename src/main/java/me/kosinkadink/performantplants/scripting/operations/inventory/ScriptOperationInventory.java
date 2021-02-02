package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationInventory extends ScriptOperationNoOptimize {

    public ScriptOperationInventory(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.INVENTORY;
    }
}
