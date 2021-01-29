package me.kosinkadink.performantplants.scripting.operations.cast;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationUnary;

public abstract class ScriptOperationCast extends ScriptOperationUnary {

    public ScriptOperationCast(ScriptBlock input) {
        super(input);
    }

    @Override
    protected void validateInputs() {
        switch (getInput().getType()) {
            case BOOLEAN:
            case LONG:
            case DOUBLE:
            case STRING:
                break;
            default:
                throw new IllegalArgumentException("Cast operation only supports BOOLEAN, LONG, DOUBLE, and STRING");
        }
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.CAST;
    }

}
