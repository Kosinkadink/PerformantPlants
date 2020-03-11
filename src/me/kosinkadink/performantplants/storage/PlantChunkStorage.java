package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;

public class PlantChunkStorage {

    private Main main;
    private String world;
    private HashMap<ChunkLocation, PlantChunk> plantChunks = new HashMap<>();
    private HashSet<BlockLocation> blockLocationsToDelete = new HashSet<>();

    public PlantChunkStorage(Main mainClass, String world) {
        main = mainClass;
        this.world = world;
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
        // add metadata to block
        MetadataHelper.setPlantBlockMetadata(main, block.getBlock());
        // remove block from removal set
        removeBlockFromRemoval(block);
        main.getLogger().info("Added PlantBlock: " + block.toString() + " to world: "
                + getWorldName()
        );
    }

    public void removePlantBlock(PlantBlock block) {
        // get plant chunk
        PlantChunk plantChunk = plantChunks.get(new ChunkLocation(block));
        // if exists, remove plantBlock from plantChunk
        if (plantChunk != null) {
            plantChunk.removePlantBlock(block);
            // add block to removal set
            addBlockForRemoval(block);
            // remove metadata from block
            MetadataHelper.removePlantBlockMetadata(main, block.getBlock());
            main.getLogger().info("Removed PlantBlock: " + block.toString()+ " from world: "
                    + getWorldName()
            );
            // if no more plant blocks in plantChunk, remove plantChunk
            if (plantChunk.isEmpty()) {
                removePlantChunk(plantChunk);
            }
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

    public PlantBlock getPlantBlock(Block block) {
        return getPlantBlock(new BlockLocation(block));
    }

    public void addPlantChunk(PlantChunk chunk) {
        if (chunk.isChunkLoaded() && !chunk.isLoaded()) {
            chunk.load();
        }
        plantChunks.put(chunk.getLocation(), chunk);
        main.getLogger().info("Added PlantChunk: " + chunk.toString());
    }

    public void removePlantChunk(PlantChunk chunk) {
        plantChunks.remove(chunk.getLocation());
        main.getLogger().info("Removed PlantChunk: " + chunk.toString());
    }

    void addBlockForRemoval(PlantBlock block) {
        blockLocationsToDelete.add(block.getLocation());
    }

    void removeBlockFromRemoval(PlantBlock block) {
        blockLocationsToDelete.remove(block.getLocation());
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
