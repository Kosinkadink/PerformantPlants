package me.kosinkadink.performantplants.executors;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.commands.PPCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PPCommandExecutor implements TabExecutor {

    private PerformantPlants performantPlants;

    public PPCommandExecutor(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
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
        for (PPCommand ppCommand : performantPlants.getCommandManager().getRegisteredCommands()) {
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

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        List<PPCommand> subCommands = performantPlants.getCommandManager().getRegisteredCommands();
        List<String> possibleOptions = new ArrayList<>();
        // fill up possible options
        for (PPCommand subCommand : subCommands) {
            // skip command if sender doesn't have permission to use it
            if (!commandSender.hasPermission(subCommand.getPermission())) {
                continue;
            }
            // if command name exceeds or equal to arguments provided, check for options
            if (subCommand.getCommandNameWords().length >= args.length) {
                for (int i = 0; i < args.length; i++) {
                    if (i < args.length-1) {
                        if (!subCommand.getCommandNameWords()[i].equals(args[i])) {
                            break;
                        }
                    } else {
                        if (subCommand.getCommandNameWords()[i].startsWith(args[i])) {
                            possibleOptions.add(subCommand.getCommandNameWords()[i]);
                        }
                    }
                }
            }
            // otherwise, check if it is a match
            else {
                boolean matches = true;
                // check if command words match arguments provided
                for (int i = 0; i < subCommand.getCommandNameWords().length; i++) {
                    if (!args[i].equalsIgnoreCase(subCommand.getCommandNameWords()[i])) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return subCommand.getTabCompletionResult(commandSender, args);
                }
            }
        }
        return possibleOptions;
    }
}
