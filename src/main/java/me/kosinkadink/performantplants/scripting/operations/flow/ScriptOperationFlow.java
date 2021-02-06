package me.kosinkadink.performantplants.scripting.operations.flow;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptOperation;

import javax.annotation.Nonnull;

public abstract class ScriptOperationFlow extends ScriptOperation {

    public ScriptOperationFlow(ScriptBlock... inputs) {
        super(inputs);
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FLOW;
    }

}
