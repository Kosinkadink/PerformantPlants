package me.kosinkadink.performantplants.scripting.operations.type;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptOperation;

public abstract class ScriptOperationBinary extends ScriptOperation {

    public ScriptOperationBinary(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    public ScriptBlock getLeft() {
        return inputs[0];
    }

    public ScriptBlock getRight() {
        return inputs[1];
    }

}
