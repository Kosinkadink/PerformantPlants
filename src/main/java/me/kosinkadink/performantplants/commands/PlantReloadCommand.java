package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlantReloadCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantReloadCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "reload" },
                "Reloads plant configs and all plant blocks.",
                "/pp reload",
                "performantplants.reload",
                0,
                0);
        performantPlants = performantPlantsClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // send start message
        String startMessage = "Reloading PerformantPlants, this might take a bit...";
        performantPlants.getLogger().info(startMessage);
        if (commandSender instanceof Player) {
            commandSender.sendMessage(startMessage);
        }
        // perform reload
        performantPlants.manualReload();
        // send end message
        String endMessage = "PerformantPlants reload completed.";
        performantPlants.getLogger().info(endMessage);
        if (commandSender instanceof Player) {
            commandSender.sendMessage(endMessage);
        }
    }
}
