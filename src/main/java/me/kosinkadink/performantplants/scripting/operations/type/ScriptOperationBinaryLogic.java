package me.kosinkadink.performantplants.scripting.operations.type;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptType;

public abstract class ScriptOperationBinaryLogic extends ScriptOperationBinary {

    public ScriptOperationBinaryLogic(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {

    }

}
