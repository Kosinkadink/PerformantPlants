package me.kosinkadink.performantplants.commands;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class PlantChunksCommand extends PPCommand {

    private PerformantPlants performantPlants;

    public PlantChunksCommand(PerformantPlants performantPlantsClass) {
        super(new String[] { "chunks" },
                "Returns total amount of plant chunks in world.",
                "/pp chunks",
                "performantplants.chunks",
                0,
                0);
        performantPlants = performantPlantsClass;
    }

    @Override
    public void executeCommand(CommandSender commandSender, List<String> argList) {
        for (Map.Entry<String, PlantChunkStorage> storageMap : performantPlants.getPlantManager().getPlantChunkStorageMap().entrySet()) {
            int loadedCount = 0;
            int unloadedCount = 0;
            PlantChunkStorage storage = storageMap.getValue();
            for (Map.Entry<ChunkLocation, PlantChunk> entry : storage.getPlantChunks().entrySet()) {
                if (entry.getValue().isLoaded()) {
                    loadedCount++;
                }
                else {
                    unloadedCount++;
                }
            }
            commandSender.sendMessage(
                    "World '" + storage.getWorldName()
                        + "' Chunks: " + (loadedCount+unloadedCount)
                        + " Loaded: " + loadedCount
                        + " Unloaded: " + unloadedCount);
        }
    }
}
