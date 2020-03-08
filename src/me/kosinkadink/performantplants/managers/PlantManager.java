package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import org.bukkit.Chunk;

import java.util.HashMap;

public class PlantManager {

    private Main main;
    private static HashMap<ChunkLocation, PlantChunk> plantChunks = new HashMap<>();

    public PlantManager(Main mainClass) {
        main = mainClass;
    }

    public HashMap<ChunkLocation, PlantChunk> getPlantChunks() {
        return plantChunks;
    }

    public void addPlantBlock(PlantBlock block) {
        ChunkLocation chunkLocation = new ChunkLocation(block);
        // get plantChunk
        PlantChunk plantChunk = plantChunks.get(chunkLocation);
        // if plantChunk doesn't exist yet, create it and add it to plantChunks
        if (plantChunk == null) {
            plantChunk = new PlantChunk(chunkLocation);
            addPlantChunk(plantChunk);
        }
        // add plantBlock to plantChunk
        plantChunk.addPlantBlock(block);
        main.getLogger().info("Added PlantBlock: " + block.toString());
    }

    public void removePlantBlock(PlantBlock block) {
        // get plant chunk
        PlantChunk plantChunk = plantChunks.get(new ChunkLocation(block));
        // if exists, remove plantBlock from plantChunk
        if (plantChunk != null) {
            plantChunk.removePlantBlock(block);
        }
    }

    public PlantChunk getPlantChunk(ChunkLocation chunkLocation) {
        return plantChunks.get(chunkLocation);
    }

    public PlantChunk getPlantChunk(Chunk chunk) {
        return getPlantChunk(new ChunkLocation(chunk));
    }

    public PlantBlock getPlantBlock(BlockLocation blockLocation) {
        PlantChunk plantChunk = getPlantChunk(new ChunkLocation(blockLocation));
        if (plantChunk != null) {
            return plantChunk.getPlantBlock(blockLocation);
        }
        return null;
    }

    private void addPlantChunk(PlantChunk plantChunk) {
        if (plantChunk.isChunkLoaded() && !plantChunk.isLoaded()) {
            plantChunk.load();
        }
        plantChunks.put(plantChunk.getLocation(), plantChunk);
    }

}
