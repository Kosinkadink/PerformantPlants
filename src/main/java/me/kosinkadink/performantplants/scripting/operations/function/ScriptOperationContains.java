package me.kosinkadink.performantplants.scripting.operations.function;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;

import javax.annotation.Nonnull;

public class ScriptOperationContains extends ScriptOperationBinary {

    public ScriptOperationContains(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) {
        ScriptResult leftInstance = getLeft().loadValue(context);
        ScriptResult rightInstance = getRight().loadValue(context);
        return new ScriptResult(leftInstance.getStringValue().contains(rightInstance.getStringValue()));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() {
        if (getLeft().getType() != ScriptType.STRING) {
            throw new IllegalArgumentException("Contains operation only supports ScriptType STRING");
        }
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.FUNCTION;
    }

}
