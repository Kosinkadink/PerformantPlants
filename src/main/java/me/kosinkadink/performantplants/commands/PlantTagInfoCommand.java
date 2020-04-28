package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PlantTagInfoCommand extends PPCommand {

    private Main main;

    public PlantTagInfoCommand(Main mainClass) {
        super(new String[] { "tag", "info" },
                "Show list of plantIds for specified tag.",
                "/pp tag info <tag-id>",
                "performantplants.tag.info",
                1,
                1);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        StatisticsTagStorage storage = main.getStatisticsManager().getPlantTag(tagId);
        if (storage == null) {
            commandSender.sendMessage(String.format("Tag '%s' not recognized", tagId));
            return;
        }
        commandSender.sendMessage(String.format("Tag '%s' contains: %s", tagId, storage.getAllPlantIds()));
    }
}
