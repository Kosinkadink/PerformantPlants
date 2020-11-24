package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlantHelpCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantHelpCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "help" },
                "List usage and descriptions for available commands.",
                "/pp help",
                "performantplants.help",
                0,
                0);
        performantPlants = performantPlantsClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        commandSender.sendMessage(String.format("%s ----%s PerformantPlants Help%s ----",
                ChatColor.AQUA, ChatColor.GREEN, ChatColor.AQUA));
        for (PPCommand command : performantPlants.getCommandManager().getRegisteredCommands()) {
            if (commandSender instanceof Player) {
                // if player does not have permission to use command, don't list it
                if (!commandSender.hasPermission(command.getPermission())) {
                    continue;
                }
            }
            commandSender.sendMessage(String.format("%s%s %s- %s%s",
                    ChatColor.GREEN, command.getUsage(), ChatColor.AQUA,
                    ChatColor.LIGHT_PURPLE, command.getDescription()));
        }
    }
}
