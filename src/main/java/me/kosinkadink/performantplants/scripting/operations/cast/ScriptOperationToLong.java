package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptType;

public class ScriptOperationToLong extends ScriptOperationCast {

    public ScriptOperationToLong(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

}
