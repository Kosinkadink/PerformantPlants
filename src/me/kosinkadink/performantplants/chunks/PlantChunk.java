package me.kosinkadink.performantplants.chunks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;

import java.util.HashMap;

public class PlantChunk {

    private final ChunkLocation location;
    private HashMap<BlockLocation, PlantBlock> plantBlocks = new HashMap<>();
    private boolean loaded = false;

    public PlantChunk(ChunkLocation chunkLocation) {
        location = chunkLocation;
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
        // start task for plantBlock
        plantBlock.startTask();
    }

    public void removePlantBlock(PlantBlock plantBlock) {
        // pause task for plantBlock
        plantBlock.pauseTask();
        // remove plantBlock from hash map
        plantBlocks.remove(plantBlock.getLocation());
    }

    public PlantBlock getPlantBlock(BlockLocation blockLocation) {
        return plantBlocks.get(blockLocation);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        // start up task for each plantBlock
        plantBlocks.forEach((blockLocation, plantBlock) -> plantBlock.startTask());
        loaded = true;
    }

    public void unload() {
        // pause task for each plantBlock
        plantBlocks.forEach((blockLocation, plantBlock) -> plantBlock.pauseTask());
        loaded = false;
    }
}
