package me.kosinkadink.performantplants.scripting.operations.compare;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;

import javax.annotation.Nonnull;

public abstract class ScriptOperationCompare extends ScriptOperationBinary {

    public ScriptOperationCompare(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    protected void validateInputs() {
        if (getLeft() == null || getRight() == null) {
            throw new IllegalArgumentException("One or more values were null");
        }
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.COMPARE;
    }

}
