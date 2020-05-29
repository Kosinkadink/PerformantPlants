package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptType;

public class ScriptOperationToString extends ScriptOperationCast {

    public ScriptOperationToString(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void setType() {
        type = ScriptType.STRING;
    }

}
