package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlantTagRemoveCommand extends PPCommand {

    private Main main;

    public PlantTagRemoveCommand(Main mainClass) {
        super(new String[] { "tag", "remove" },
                "Removes plant ids from tag; unregisters tag if no plant ids remaining.",
                "/pp tag remove <tag-id> <plant-ids,comma,separated>",
                "performantplants.tag.remove",
                2,
                2);
        main = mainClass;
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
        StatisticsTagStorage storage = main.getStatisticsManager().getPlantTag(tagId);
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
            if (main.getStatisticsManager().removePlantId(tagId, plantId) != null) {
                plantIdsRemoved.add(plantId);
            } else {
                plantIdsNotRemoved.add(plantId);
            }
        }
        if (plantIdsRemoved.isEmpty()) {
            commandSender.sendMessage("None of the plant ids were found stored in the tag; none removed");
            return;
        }
        boolean allRemoved = main.getStatisticsManager().getPlantTag(tagId) == null;
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
