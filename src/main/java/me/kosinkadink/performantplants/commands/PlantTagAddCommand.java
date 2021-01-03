package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlantTagAddCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagAddCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "add" },
                "Add plant ids to existing tag.",
                "/pp tag add <tag-id> <plant-ids,comma,separated>",
                "performantplants.tag.add",
                2,
                2);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        // if on first argument, then return list of plant tags
        if (args.length == commandNameWords.length+1) {
            String tagId = args[commandNameWords.length];
            return performantPlants.getStatisticsManager().getAllPlantTags()
                    .stream().filter(id -> id.startsWith(tagId)).collect(Collectors.toList());
        }
        // if on second argument, then return list of plant-ids (continue to show when comma-separated
        if (args.length == commandNameWords.length+2) {
            String fullPlantIdString = args[commandNameWords.length+1];
            return getTabCompletionListOfPlantIds(fullPlantIdString, performantPlants);
        }
        return emptyList;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        String plantIdsCommas = argList.get(1);
        if (tagId.isEmpty()) {
            commandSender.sendMessage("Tag id cannot be empty");
            return;
        }
        // see if tag exists
        StatisticsTagStorage storage = performantPlants.getStatisticsManager().getPlantTag(tagId);
        if (storage == null) {
            commandSender.sendMessage(String.format("Tag '%s' does not exist", tagId));
            return;
        }
        // get plant ids to be added
        String[] plantIdList = plantIdsCommas.split(",");
        if (plantIdList.length == 0) {
            commandSender.sendMessage("No plant ids provided");
            return;
        }
        ArrayList<String> plantIdsAdded = new ArrayList<>();
        ArrayList<String> plantIdsNotAdded = new ArrayList<>();
        for (String plantId : plantIdList) {
            if (performantPlants.getPlantTypeManager().getPlantItemById(plantId) != null) {
                performantPlants.getStatisticsManager().addPlantId(tagId, plantId);
                plantIdsAdded.add(plantId);
            } else {
                plantIdsNotAdded.add(plantId);
            }
        }
        // report result to user
        if (plantIdsAdded.isEmpty()) {
            commandSender.sendMessage("Could not recognize any of the provided plant ids");
            return;
        }
        if (!plantIdsNotAdded.isEmpty()) {
            commandSender.sendMessage(String.format("Partial success; added plant ids: %s, but following plant ids do not exist: %s",
                    plantIdsAdded.toString(),
                    plantIdsNotAdded.toString())
            );
            return;
        }
        commandSender.sendMessage(String.format("Added all plant ids successfully: %s", plantIdsAdded));
    }
}
