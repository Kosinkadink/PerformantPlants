package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class PlantInfoCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantInfoCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "info" },
                "Show plant info.",
                "/pp info <plant-id>",
                "performantplants.info",
                1,
                1);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        if (args.length == commandNameWords.length+1) {
            return getTabCompletionPlantIds(args[commandNameWords.length], performantPlants, false);
        }
        return Collections.emptyList();
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String plantId = argList.get(0);
        // get plant
        Plant plant = performantPlants.getPlantTypeManager().getPlantById(plantId);
        if (plant == null) {
            commandSender.sendMessage(String.format("Plant '%s' not recognized", plantId));
            return;
        }
        // print plant info
        String plantInfo = String.format("Plant Id: %s", plantId);
        plantInfo += String.format("\nBuy Price: %.2f", plant.getItem().getBuyPrice());
        plantInfo += String.format("\nSell Price: %.2f", plant.getItem().getSellPrice());
        if (plant.hasSeed()) {
            plantInfo += "\nSeed:";
            plantInfo += String.format("\n  Buy Price: %.2f", plant.getSeedItem().getBuyPrice());
            plantInfo += String.format("\n  Sell Price: %.2f", plant.getSeedItem().getSellPrice());
        }
        // send plant info
        commandSender.sendMessage(plantInfo);
    }
}
