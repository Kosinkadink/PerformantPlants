package me.kosinkadink.performantplants.executors;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.commands.PPCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;

public class PPCommandExecutor implements CommandExecutor {

    private Main main;

    public PPCommandExecutor(Main mainClass) {
        main = mainClass;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // if no args, don't bother doing anything
        if (args.length == 0) {
            commandSender.sendMessage("No command supplied to PerformantPlants");
            return true;
        }
        // find if args match a set of command words
        boolean matches;
        for (PPCommand ppCommand : main.getCommandManager().getRegisteredCommands()) {
            matches = true;
            // if command words are longer than args, go to next command
            if (ppCommand.getCommandNameWords().length > args.length) {
                continue;
            }
            // check if command words match arguments provided
            for (int i = 0; i < ppCommand.getCommandNameWords().length; i++) {
                if (!args[i].equalsIgnoreCase(ppCommand.getCommandNameWords()[i])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                // check if player has permission to use command
                if (commandSender.hasPermission(ppCommand.getPermission())) {
                    // check if arg length satisfies command
                    if (args.length >= ppCommand.getCommandNameWords().length + ppCommand.getMinArgs() &&
                            args.length <= ppCommand.getCommandNameWords().length + ppCommand.getMaxArgs()) {
                        // pass in only relevant arguments
                        ArrayList<String> relevantArgs = new ArrayList<>(Arrays.asList(args).subList(ppCommand.getCommandNameWords().length, args.length));
                        ppCommand.executeCommand(commandSender, relevantArgs);
                        return true;
                    }
                    commandSender.sendMessage(ppCommand.getUsage());
                    return true;
                }
                commandSender.sendMessage("You don't have permission to use this PerformantPlants command");
                return true;
            }
        }
        commandSender.sendMessage("Command not recognized by PerformantPlants");
        return true;
    }
}
