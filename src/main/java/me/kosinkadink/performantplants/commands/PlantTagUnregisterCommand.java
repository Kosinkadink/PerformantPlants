package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.statistics.StatisticsTagItem;
import me.kosinkadink.performantplants.storage.StatisticsTagStorage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PlantTagUnregisterCommand extends PPCommand {

    private Main main;

    public PlantTagUnregisterCommand(Main mainClass) {
        super(new String[] { "tag", "unregister" },
                "Unregister tag; cannot be undone.",
                "/pp tag unregister <tag-id>",
                "performantplants.tag.unregister",
                1,
                1);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        String tagId = argList.get(0);
        // see if registered
        boolean removed = main.getStatisticsManager().unregisterPlantTag(tagId);
        if (!removed) {
            commandSender.sendMessage(String.format("Tag '%s' not found", tagId));
            return;
        }
        commandSender.sendMessage(String.format("Unregistered Tag '%s'", tagId));
    }
}
