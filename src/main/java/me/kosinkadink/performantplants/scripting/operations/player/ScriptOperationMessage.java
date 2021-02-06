package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;
import me.kosinkadink.performantplants.util.ScriptHelper;
import me.kosinkadink.performantplants.util.TextHelper;

import javax.annotation.Nonnull;

public class ScriptOperationMessage extends ScriptOperationNoOptimize {

    public ScriptOperationMessage(ScriptBlock message) {
        super(message);
    }

    public ScriptBlock getMessage() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            String message = getMessage().loadValue(context).getStringValue();
            context.getPlayer().sendMessage(TextHelper.translateAlternateColorCodes(message));
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
