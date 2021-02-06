package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.command.ConsoleCommandSender;

import javax.annotation.Nonnull;

public class ScriptOperationConsoleCommand extends ScriptOperationAction {

    public ScriptOperationConsoleCommand(ScriptBlock command) {
        super(command);
    }

    public ScriptBlock getCommand() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        ScriptResult commandResult = getCommand().loadValue(context);
        String commandString = commandResult.getStringValue();
        if (!commandResult.isHasPlaceholder()) {
            commandString = PlaceholderHelper.setVariablesAndPlaceholders(context, commandString);
        }
        try {
            ConsoleCommandSender console = PerformantPlants.getInstance().getServer().getConsoleSender();
            PerformantPlants.getInstance().getServer().dispatchCommand(console, commandString);
            return ScriptResult.TRUE;
        } catch (IllegalArgumentException e) {
            // do nothing, just make sure this doesn't cause runtime errors
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
