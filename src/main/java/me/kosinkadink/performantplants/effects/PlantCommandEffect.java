package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.PlaceholderHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class PlantCommandEffect extends PlantEffect {

    private ScriptBlock command;
    private ScriptBlock console = ScriptResult.TRUE;

    public PlantCommandEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        ScriptResult commandResult = command.loadValue(context);
        String commandString = commandResult.getStringValue();
        if (!commandResult.isHasPlaceholder()) {
            commandString = PlaceholderHelper.setVariablesAndPlaceholders(context, commandString);
        }
        if (isConsole(context)) {
            try {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, commandString);
            } catch (IllegalArgumentException e) {
                // do nothing, just make sure this doesn't cause bad stuff to happen
            }
        } else {
            context.getPlayer().sendMessage("/" + commandString);
        }
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        ScriptResult commandResult = command.loadValue(context);
        String commandString = commandResult.getStringValue();
        if (!commandResult.isHasPlaceholder()) {
            commandString = PlaceholderHelper.setVariablesAndPlaceholders(context, commandString);
        }
        try {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, commandString);
        } catch (IllegalArgumentException e) {
            // do nothing, just make sure this doesn't cause bad stuff to happen
        }
    }

    public ScriptBlock getCommand() {
        return command;
    }

    public String getCommandValue(ExecutionContext context) {
        return command.loadValue(context).getStringValue();
    }

    public void setCommand(ScriptBlock command) {
        this.command = command;
    }

    public ScriptBlock getConsole() {
        return console;
    }

    public boolean isConsole(ExecutionContext context) {
        return console.loadValue(context).getBooleanValue();
    }

    public void setConsole(ScriptBlock console) {
        this.console = console;
    }

}
