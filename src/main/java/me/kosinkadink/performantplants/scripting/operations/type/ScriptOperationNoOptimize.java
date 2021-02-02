package me.kosinkadink.performantplants.scripting.operations.type;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptOperation;

public abstract class ScriptOperationNoOptimize extends ScriptOperation {

    public ScriptOperationNoOptimize(ScriptBlock... inputs) {
        super(inputs);
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

}
