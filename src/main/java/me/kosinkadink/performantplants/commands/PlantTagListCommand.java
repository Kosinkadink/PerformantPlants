package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlantTagListCommand extends PPCommand {

    private Main main;

    public PlantTagListCommand(Main mainClass) {
        super(new String[] { "tag", "list" },
                "Show list of all registered tags.",
                "/pp tag list",
                "performantplants.tag.list",
                0,
                0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        ArrayList<String> tags = main.getStatisticsManager().getAllPlantTags();
        if (tags.isEmpty()) {
            commandSender.sendMessage("No tags have been registered.");
            return;
        }
        commandSender.sendMessage(String.format("Registered tags: %s", tags));
    }
}
