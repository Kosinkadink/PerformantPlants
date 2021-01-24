package me.kosinkadink.performantplants.chunks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantChunkLoadedEvent;
import me.kosinkadink.performantplants.events.PlantChunkUnloadedEvent;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;

import java.util.concurrent.ConcurrentHashMap;

public class PlantChunk {

    private final ChunkLocation location;
    private final ConcurrentHashMap<BlockLocation, PlantBlock> plantBlocks = new ConcurrentHashMap<>();
    private boolean loaded = false;
    private boolean loadedSinceSave = false;

    public PlantChunk(ChunkLocation chunkLocation) {
        location = chunkLocation;
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public ConcurrentHashMap<BlockLocation, PlantBlock> getPlantBlocks() {
        return plantBlocks;
    }

    public void addPlantBlock(PlantBlock plantBlock) {
        // mark as loaded since save
        loadedSinceSave = true;
        // add plantBlock to hash map
        plantBlocks.put(plantBlock.getLocation(), plantBlock);
    }

    public boolean removePlantBlock(PlantBlock plantBlock) {
        // mark as loaded since save
        loadedSinceSave = true;
        // remove plantBlock from hash map
        PlantBlock removed = plantBlocks.remove(plantBlock.getLocation());
        return removed != null;
    }

    public PlantBlock getPlantBlock(BlockLocation blockLocation) {
        return plantBlocks.get(blockLocation);
    }

    public boolean isEmpty() {
        return plantBlocks.isEmpty();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean wasLoadedSinceSave() {
        return loadedSinceSave;
    }

    public void clearLoadedSinceSave() {
        loadedSinceSave = false;
    }

    public boolean isChunkLoaded() {
        // using getWorld().isChunkLoaded because it does not load the chunk to check it
        return location.getWorld().isChunkLoaded(location.getX(), location.getZ());
    }

    public void load(PerformantPlants performantPlants) {
        // start up task for each plantBlock
        plantBlocks.forEach((blockLocation, plantBlock) -> performantPlants.getPlantManager().startGrowthTask(plantBlock));
        loaded = true;
        // mark as loaded since save
        loadedSinceSave = true;
        // send loaded event
        if (performantPlants.isPPEnabled()) {
            performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                    performantPlants.getServer().getPluginManager().callEvent(new PlantChunkLoadedEvent(this))
            );
        }
    }

    public void unload(PerformantPlants performantPlants) {
        // pause task for each plantBlock
        plantBlocks.forEach((blockLocation, plantBlock) -> performantPlants.getPlantManager().pauseGrowthTask(plantBlock));
        loaded = false;
        // send unloaded event
        if (performantPlants.isPPEnabled()) {
             performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                    performantPlants.getServer().getPluginManager().callEvent(new PlantChunkUnloadedEvent(this))
            );
        }
    }

    @Override
    public String toString() {
        return "PlantChunk @ " + location.toString();
    }

}
