package me.kosinkadink.performantplants.scripting.operations.type;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptOperation;

public abstract class ScriptOperationUnary extends ScriptOperation {

    public ScriptOperationUnary(ScriptBlock input) {
        super(input);
    }

    public ScriptBlock getInput() {
        return inputs[0];
    }

}
