package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PlantTagUnregisterCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagUnregisterCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "unregister" },
                "Unregister tag; cannot be undone.",
                "/pp tag unregister <tag-id>",
                "performantplants.tag.unregister",
                1,
                1);
        performantPlants = performantPlantsClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        // see if registered
        boolean removed = performantPlants.getStatisticsManager().unregisterPlantTag(tagId);
        if (!removed) {
            commandSender.sendMessage(String.format("Tag '%s' not found", tagId));
            return;
        }
        commandSender.sendMessage(String.format("Unregistered Tag '%s'", tagId));
    }
}
