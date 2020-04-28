package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlantTagRegisterCommand extends PPCommand {

    private Main main;

    public PlantTagRegisterCommand(Main mainClass) {
        super(new String[] { "tag", "register" },
                "Register tag with initial plant ids; adds plant ids to existing tag if applicable.",
                "/pp tag register <tag-id> <plant-ids,comma,separated>",
                "performantplants.tag.register",
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
        // get plant ids to be added
        String[] plantIdList = plantIdsCommas.split(",");
        if (plantIdList.length == 0) {
            commandSender.sendMessage("No plantIds provided");
            return;
        }
        ArrayList<String> plantIdsAdded = new ArrayList<>();
        ArrayList<String> plantIdsNotAdded = new ArrayList<>();
        for (String plantId : plantIdList) {
            if (main.getPlantTypeManager().getPlantById(plantId) != null) {
                main.getStatisticsManager().addPlantId(tagId, plantId, true);
                plantIdsAdded.add(plantId);
            } else {
                plantIdsNotAdded.add(plantId);
            }
        }
        // report result to user
        if (plantIdsAdded.isEmpty()) {
            commandSender.sendMessage("Could not recognize any of the provided plantIds");
            return;
        }
        if (!plantIdsNotAdded.isEmpty()) {
            commandSender.sendMessage(String.format("Partial success; added plantIds: %s, but following plantIds do not exist: %s", plantIdsAdded.toString(), plantIdsNotAdded.toString()));
            return;
        }
        commandSender.sendMessage(String.format("Added all plantIds successfully: %s", plantIdsAdded));
    }
}
