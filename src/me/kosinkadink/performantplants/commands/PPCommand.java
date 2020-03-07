package me.kosinkadink.performantplants.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class PPCommand {

    private String[] commandNameWords;
    private String description;
    private String usage;
    private String permission;
    private int minArgs;
    private int maxArgs;

    PPCommand(String[] commandNameWords, String description, String usage, String permission, int minArgs, int maxArgs) {
        this.commandNameWords = commandNameWords;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public abstract void executeCommand(CommandSender commandSender, List<String> argList);

    public String[] getCommandNameWords() {
        return commandNameWords;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }
}
