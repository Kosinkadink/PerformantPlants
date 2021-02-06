package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptCategory;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;

import javax.annotation.Nonnull;

public abstract class ScriptOperationPlayer extends ScriptOperationNoOptimize {

    public ScriptOperationPlayer(ScriptBlock... inputs) {
        super(inputs);
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }

}
