package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.DropHelper;

import javax.annotation.Nonnull;

public class ScriptOperationDoDrops extends ScriptOperationAction {

    private final DropStorage dropStorage;

    public ScriptOperationDoDrops(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isLocationPossible()) {
            DropHelper.performDrops(dropStorage, context.getLocation(), context);
            return ScriptResult.TRUE;
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
