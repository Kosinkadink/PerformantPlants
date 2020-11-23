package me.kosinkadink.performantplants.executors;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.commands.PPCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PPCommandExecutor implements TabExecutor {

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
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            List<PPCommand> subCommands = main.getCommandManager().getRegisteredCommands();
            List<String> possibleSubCommands = new ArrayList<>();
            for (PPCommand subCommand : subCommands) {
                subCommand.getCommandNameWords();
                possibleSubCommands.add(subCommand.getCommandNameWords()[0]);
            }

            return possibleSubCommands;

        }

        return null;

    }
}
