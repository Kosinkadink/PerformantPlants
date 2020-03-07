package me.kosinkadink.performantplants.chunks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;

import java.util.HashMap;

public class PlantChunk {

    private final ChunkLocation location;
    private HashMap<BlockLocation, PlantBlock> plantBlocks;
    private boolean loaded = false;

    PlantChunk(ChunkLocation chunkLocation) {
        location = chunkLocation;
        plantBlocks = new HashMap<>();
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public HashMap<BlockLocation, PlantBlock> getPlantBlocks() {
        return plantBlocks;
    }

    public void addPlantBlock(PlantBlock plantBlock) {
        // add plantBlock to hash map
        plantBlocks.put(plantBlock.getLocation(), plantBlock);
    }

    public void removePlantBlock(PlantBlock plantBlock) {
        // remove plantBlock from hash map
        plantBlocks.remove(plantBlock.getLocation());
    }

    public void load() {
        loaded = true;
    }

    public void unload() {
        loaded = false;
    }
}
