package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlantReloadCommand extends PPCommand {

    private Main main;

    public PlantReloadCommand(Main mainClass) {
        super(new String[] { "reload" },
                "Reloads plant configs and all plant blocks.",
                "/pp reload",
                "performantplants.reload",
                0,
                0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        // send start message
        String startMessage = "Reloading PerformantPlants, this might take a bit...";
        main.getLogger().info(startMessage);
        if (commandSender instanceof Player) {
            commandSender.sendMessage(startMessage);
        }
        // perform reload
        main.manualReload();
        // send end message
        String endMessage = "PerformantPlants reload completed.";
        main.getLogger().info(endMessage);
        if (commandSender instanceof Player) {
            commandSender.sendMessage(endMessage);
        }
    }
}
