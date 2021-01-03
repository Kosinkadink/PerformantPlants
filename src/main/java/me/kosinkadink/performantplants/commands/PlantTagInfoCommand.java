package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class PlantTagInfoCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagInfoCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "info" },
                "Show list of plant ids for specified tag.",
                "/pp tag info <tag-id>",
                "performantplants.tag.info",
                1,
                1);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        if (args.length == commandNameWords.length+1) {
            String tagId = args[commandNameWords.length];
            return performantPlants.getStatisticsManager().getAllPlantTags()
                    .stream().filter(id -> id.startsWith(tagId)).collect(Collectors.toList());
        }
        return emptyList;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        StatisticsTagStorage storage = performantPlants.getStatisticsManager().getPlantTag(tagId);
        if (storage == null) {
            commandSender.sendMessage(String.format("Tag '%s' not recognized", tagId));
            return;
        }
        commandSender.sendMessage(String.format("Tag '%s' contains: %s", tagId, storage.getAllPlantIds()));
    }
}
