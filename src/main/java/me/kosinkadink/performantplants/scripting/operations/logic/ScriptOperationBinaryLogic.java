package me.kosinkadink.performantplants.scripting.operations.logic;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationBinary;

import javax.annotation.Nonnull;

public abstract class ScriptOperationBinaryLogic extends ScriptOperationBinary {

    public ScriptOperationBinaryLogic(ScriptBlock left, ScriptBlock right) {
        super(left, right);
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.LOGIC;
    }

}
