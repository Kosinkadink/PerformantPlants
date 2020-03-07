package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TotalPlantChunksCommand extends PPCommand {

    private Main main;

    public TotalPlantChunksCommand(Main mainClass) {
        super(new String[] { "chunks" }, "Returns total amount of plant chunks in world.", "/pp chunks", "performantplants.chunks", 0, 0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        commandSender.sendMessage("Vibe check passed.");
    }
}
