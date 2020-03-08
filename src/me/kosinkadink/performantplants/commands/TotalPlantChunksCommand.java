package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TotalPlantChunksCommand extends PPCommand {

    private Main main;

    public TotalPlantChunksCommand(Main mainClass) {
        super(new String[] { "chunks" },
                "Returns total amount of plant chunks in world.",
                "/pp chunks",
                "performantplants.chunks",
                0,
                0);
        main = mainClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        int loadedCount = 0;
        int unloadedCount = 0;
        for (Map.Entry<ChunkLocation, PlantChunk> entry : main.getPlantManager().getPlantChunks().entrySet()) {
            if (entry.getValue().isLoaded()) {
                loadedCount++;
            }
            else {
                unloadedCount++;
            }
        }
        commandSender.sendMessage(
                "Chunks: " + (loadedCount+unloadedCount) + "\n"
                + "Loaded: " + loadedCount + "\n"
                + "Unloaded: " + unloadedCount);
    }
}
