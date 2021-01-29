package me.kosinkadink.performantplants.scripting.operations.math;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;

import javax.annotation.Nonnull;

public abstract class ScriptOperationBinaryMath extends ScriptOperationBinary {

    public ScriptOperationBinaryMath(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    protected void validateInputs() {
        ScriptBlock left = getLeft();
        ScriptBlock right = getRight();
        if (left == null || right == null ||
                !(left.getType() == ScriptType.LONG || left.getType() == ScriptType.DOUBLE || left.getType() == ScriptType.BOOLEAN) ||
                !(right.getType() == ScriptType.LONG || right.getType() == ScriptType.DOUBLE || right.getType() == ScriptType.BOOLEAN)) {
            throw new IllegalArgumentException("BinaryMath operation only supports ScriptType LONG, DOUBLE, and BOOLEAN");
        }
    }

    @Override
    protected void setType() {
        if (getLeft().getType() != ScriptType.DOUBLE && getRight().getType() != ScriptType.DOUBLE) {
            type = ScriptType.LONG;
        } else {
            type = ScriptType.DOUBLE;
        }
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.MATH;
    }

}
