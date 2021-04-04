package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PlantChunkStorage {

    private PerformantPlants performantPlants;
    private String world;
    private ConcurrentHashMap<ChunkLocation, PlantChunk> plantChunks = new ConcurrentHashMap<>();
    private HashSet<BlockLocation> blockLocationsToDelete = new HashSet<>();

    public PlantChunkStorage(PerformantPlants performantPlantsClass, String world) {
        performantPlants = performantPlantsClass;
        this.world = world;
    }

    public void unloadAll() {
        for (PlantChunk plantChunk : plantChunks.values()) {
            if (plantChunk.isLoaded()) {
                plantChunk.unload(performantPlants);
            }
        }
    }

    public ConcurrentHashMap<ChunkLocation, PlantChunk> getPlantChunks() {
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
        // add metadata to block
        MetadataHelper.setPlantBlockMetadata(performantPlants, block);
        // remove block from removal set
        removeBlockFromRemoval(block);
    }

    public boolean removePlantBlock(PlantBlock block) {
        // get plant chunk
        PlantChunk plantChunk = plantChunks.get(new ChunkLocation(block));
        // if exists, remove plantBlock from plantChunk
        if (plantChunk != null) {
            if (!plantChunk.removePlantBlock(block)) {
                return false;
            }
            // remove anchors, if exists
            if (block.hasAnchors()) {
                performantPlants.getAnchorManager().removePlantBlockFromAnchorBlocks(block);
            }
            // add block to removal set
            addBlockForRemoval(block);
            // remove metadata from block
            MetadataHelper.removePlantBlockMetadata(performantPlants, block.getBlock());
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removed PlantBlock: " + block.toString()+ " from world: "
                    + getWorldName()
            );
            // if no more plant blocks in plantChunk, remove plantChunk
            if (plantChunk.isEmpty()) {
                removePlantChunk(plantChunk);
            }
            return true;
        }
        return false;
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

    public PlantBlock getPlantBlock(Block block) {
        return getPlantBlock(new BlockLocation(block));
    }

    public void addPlantChunk(PlantChunk chunk) {
        if (chunk.isChunkLoaded() && !chunk.isLoaded()) {
            chunk.load(performantPlants);
        }
        plantChunks.put(chunk.getLocation(), chunk);
        //if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Added PlantChunk: " + chunk.toString());
    }

    public void removePlantChunk(PlantChunk chunk) {
        plantChunks.remove(chunk.getLocation());
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Removed PlantChunk: " + chunk.toString());
    }

    void addBlockForRemoval(PlantBlock block) {
        blockLocationsToDelete.add(block.getLocation());
    }

    public void removeBlockFromRemoval(BlockLocation location) {
        blockLocationsToDelete.remove(location);
    }

    public void removeBlockFromRemoval(PlantBlock block) {
        removeBlockFromRemoval(block.getLocation());
    }

    public HashSet<BlockLocation> getBlockLocationsToDelete() {
        return blockLocationsToDelete;
    }

    void clearBlockLocationsToDelete() {
        blockLocationsToDelete.clear();
    }

    public String getWorldName() {
        return world;
    }
}
