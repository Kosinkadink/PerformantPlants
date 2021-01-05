package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlantTagRegisterCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantTagRegisterCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "tag", "register" },
                "Register tag with initial plant ids; adds plant ids to existing tag if applicable.",
                "/pp tag register <tag-id> <plant-ids,comma,separated>",
                "performantplants.tag.register",
                2,
                2);
        performantPlants = performantPlantsClass;
    }

    @Override
    public List<String> getTabCompletionResult(CommandSender commandSender, String[] args) {
        // if on first argument, then return list of plant tags + custom
        if (args.length == commandNameWords.length+1) {
            String tagId = args[commandNameWords.length];
            List<String> plantTags = performantPlants.getStatisticsManager().getAllPlantTags()
                    .stream().filter(id -> id.startsWith(tagId)).collect(Collectors.toList());
            plantTags.add("[New Plant Tag]");
            return plantTags;

        }
        // if on second argument, then return list of plant-ids (continue to show when comma-separated
        if (args.length == commandNameWords.length+2) {
            String fullPlantIdString = args[commandNameWords.length+1];
            return getTabCompletionListOfPlantIds(fullPlantIdString, performantPlants);
        }
        return Collections.emptyList();
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        String plantIdsCommas = argList.get(1);
        if (tagId.isEmpty()) {
            commandSender.sendMessage("Tag id cannot be empty");
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
                performantPlants.getStatisticsManager().addPlantId(tagId, plantId, true);
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
            commandSender.sendMessage(String.format("Partial success; added plant ids: %s, but following plant ids do not exist: %s", plantIdsAdded.toString(), plantIdsNotAdded.toString()));
            return;
        }
        commandSender.sendMessage(String.format("Added all plant ids successfully: %s", plantIdsAdded));
    }
}
