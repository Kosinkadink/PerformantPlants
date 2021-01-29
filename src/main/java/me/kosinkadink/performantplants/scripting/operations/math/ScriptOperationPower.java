package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationPower extends ScriptOperationBinaryMath {

    public ScriptOperationPower(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        // if left or right is variable, use proper temp value;
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
//        if (leftInstance.getType() != ScriptType.DOUBLE && rightInstance.getType() != ScriptType.DOUBLE) {
//            return new ScriptResult(Math.pow(leftInstance.getLongValue(), rightInstance.getLongValue()));
//        }
        return new ScriptResult(Math.pow(leftInstance.getDoubleValue(), rightInstance.getDoubleValue()));
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
//        if (getLeft().getType() != ScriptType.DOUBLE && getRight().getType() != ScriptType.DOUBLE) {
//            type = ScriptType.LONG;
//        } else {
//            type = ScriptType.DOUBLE;
//        }
    }

}
