package me.kosinkadink.performantplants.scripting.operations.random;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.RandomHelper;

public class ScriptOperationChoice extends ScriptOperation {

    private boolean containsString = false;
    private boolean containsLong = false;
    private boolean containsDouble = false;
    private boolean containsBoolean = false;

    public ScriptOperationChoice(ScriptBlock... inputs) {
        super(inputs);
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        // if only one input, use it
        if (inputs.length == 1) {
            return inputs[0].loadValue(context);
        } else {
            // otherwise generate random index and use that input
            int index = RandomHelper.generateRandomIntInRange(0, inputs.length);
            ScriptResult result = inputs[index].loadValue(context);
            // if result type does not match input, convert it
            if (result.getType() != type) {
                switch (type) {
                    case STRING:
                        result = new ScriptResult(result.getStringValue());
                    case DOUBLE:
                        result = new ScriptResult(result.getDoubleValue());
                    case LONG:
                        result = new ScriptResult(result.getLongValue());
                    default:
                        result = new ScriptResult(result.getBooleanValue());
                }
            }
            return result;
        }
    }

    @Override
    protected void setType() {
        if (containsString) {
            type = ScriptType.STRING;
        } else if (containsDouble) {
            type = ScriptType.DOUBLE;
        } else if (containsLong) {
            type = ScriptType.LONG;
        } else if (containsBoolean) {
            type = ScriptType.BOOLEAN;
        }
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Choice operation requires at least one input");
        }
        for (ScriptBlock input : inputs) {
            switch (input.getType()) {
                case STRING:
                    containsString = true;
                    break;
                case LONG:
                    containsLong = true;
                    break;
                case DOUBLE:
                    containsDouble = true;
                    break;
                case BOOLEAN:
                    containsBoolean = true;
                    break;
                case NULL:
                    throw new IllegalArgumentException("Choice operation does not allow ScriptType NULL as an input");
            }
        }
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.RANDOM;
    }

}
