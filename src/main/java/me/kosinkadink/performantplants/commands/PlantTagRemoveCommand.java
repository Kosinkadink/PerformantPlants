package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlantTagRemoveCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagRemoveCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "remove" },
                "Removes plant ids from tag; unregisters tag if no plant ids remaining.",
                "/pp tag remove <tag-id> <plant-ids,comma,separated>",
                "performantplants.tag.remove",
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
        // if on second argument, then return list of plant-ids contained in tag
        // (continue to show when comma-separated
        if (args.length == commandNameWords.length+2) {
            String tagId = args[commandNameWords.length];
            StatisticsTagStorage tagStorage = performantPlants.getStatisticsManager().getPlantTag(tagId);
            if (tagStorage != null) {
                String fullPlantIdString = args[commandNameWords.length + 1];
                // separate by commas
                String plantId;
                String existingPlantIds = "";
                int index = fullPlantIdString.lastIndexOf(",");
                if (index >= 0) {
                    if (index < fullPlantIdString.length() - 1) {
                        plantId = fullPlantIdString.substring(index+1);
                    } else {
                        plantId = "";
                    }
                    existingPlantIds = fullPlantIdString.substring(0, index + 1);
                } else {
                    plantId = fullPlantIdString;
                }
                List<String> possibleOptions = tagStorage.getAllPlantIds()
                        .stream().filter(id -> id.startsWith(plantId)).collect(Collectors.toList());
                // finalize for lambda function
                String finalExistingPlantIds = existingPlantIds;
                possibleOptions.replaceAll(plant -> finalExistingPlantIds + plant);
                return possibleOptions;
            }
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
        ArrayList<String> plantIdsRemoved = new ArrayList<>();
        ArrayList<String> plantIdsNotRemoved = new ArrayList<>();
        for (String plantId : plantIdList) {
            if (performantPlants.getStatisticsManager().removePlantId(tagId, plantId) != null) {
                plantIdsRemoved.add(plantId);
            } else {
                plantIdsNotRemoved.add(plantId);
            }
        }
        if (plantIdsRemoved.isEmpty()) {
            commandSender.sendMessage("None of the plant ids were found stored in the tag; none removed");
            return;
        }
        boolean allRemoved = performantPlants.getStatisticsManager().getPlantTag(tagId) == null;
        if (allRemoved) {
            commandSender.sendMessage("Successfully removed plant ids; no plant ids left in tag, tag is now unregistered");
            return;
        }
        if (!plantIdsNotRemoved.isEmpty()) {
            commandSender.sendMessage(String.format("Partial success; removed plant ids: %s, but following plant ids not found in tag: %s",
                    plantIdsRemoved.toString(),
                    plantIdsNotRemoved.toString())
            );
            return;
        }
        commandSender.sendMessage(String.format("Removed specified plant ids successfully: %s", plantIdsRemoved));
    }
}
