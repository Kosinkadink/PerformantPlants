package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationKick extends ScriptOperationPlayer {

    public ScriptOperationKick(ScriptBlock message) {
        super(message);
    }

    public ScriptBlock getMessage() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            context.getPlayer().kickPlayer(getMessage().loadValue(context).getStringValue());
            return ScriptResult.TRUE;
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isString(getMessage())) {
            throw new IllegalArgumentException(String.format("message must be ScriptType STRING, not %s", getMessage().getType()));
        }
    }
}
