package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlantTagListCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagListCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "list" },
                "Show list of all registered tags.",
                "/pp tag list",
                "performantplants.tag.list",
                0,
                0);
        performantPlants = performantPlantsClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        ArrayList<String> tags = performantPlants.getStatisticsManager().getAllPlantTags();
        if (tags.isEmpty()) {
            commandSender.sendMessage("No tags have been registered.");
            return;
        }
        commandSender.sendMessage(String.format("Registered tags: %s", tags));
    }
}
