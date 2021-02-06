package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationPlayerCommand extends ScriptOperationNoOptimize {

    public ScriptOperationPlayerCommand(ScriptBlock command) {
        super(command);
    }

    public ScriptBlock getCommand() {
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
            ScriptResult commandResult = getCommand().loadValue(context);
            String commandString = commandResult.getStringValue();
            if (!commandResult.isHasPlaceholder()) {
                commandString = PlaceholderHelper.setVariablesAndPlaceholders(context, commandString);
            }
            context.getPlayer().sendMessage("/" + commandString);
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
        if (!ScriptHelper.isString(getCommand())) {
            throw new IllegalArgumentException(String.format("input must be ScriptType STRING, not %s", getCommand().getType()));
        }
    }
}
