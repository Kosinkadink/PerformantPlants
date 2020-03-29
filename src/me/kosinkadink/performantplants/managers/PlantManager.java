package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.chunks.PlantChunk;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.ChunkLocation;
import me.kosinkadink.performantplants.storage.PlantChunkStorage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;

public class PlantManager {

    private Main main;
    private static HashMap<String, PlantChunkStorage> plantChunkStorageMap = new HashMap<>();

    public PlantManager(Main mainClass) {
        main = mainClass;
        createPlantChunkStorageMap();
    }

    public void createPlantChunkStorageMap() {
        for (World world : Bukkit.getWorlds()) {
            PlantChunkStorage plantChunkStorage = new PlantChunkStorage(main, world.getName());
            addPlantChunkStorage(plantChunkStorage);
        }
    }

    public void unloadAll() {
        for (PlantChunkStorage plantChunkStorage : plantChunkStorageMap.values()) {
            plantChunkStorage.unloadAll();
        }
    }

    public HashMap<String, PlantChunkStorage> getPlantChunkStorageMap() {
        return plantChunkStorageMap;
    }

    public PlantChunkStorage getPlantChunkStorage(String worldName) {
        return plantChunkStorageMap.get(worldName);
    }

    public PlantChunkStorage getPlantChunkStorage(ChunkLocation chunkLocation) {
        return plantChunkStorageMap.get(chunkLocation.getWorldName());
    }

    public void addPlantBlock(PlantBlock block) {
        ChunkLocation chunkLocation = new ChunkLocation(block);
        // get plantChunkStorage
        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(chunkLocation);
        if (plantChunkStorage == null) {
            plantChunkStorage = new PlantChunkStorage(main, chunkLocation.getWorldName());
            addPlantChunkStorage(plantChunkStorage);
        }
        plantChunkStorage.addPlantBlock(block);
        // start growth task
        startGrowthTask(block);
    }

    public void removePlantBlock(PlantBlock block) {
        // pause growth task
        pauseGrowthTask(block);
        // get plant chunk
        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(block.getLocation().getWorldName());
        if (plantChunkStorage != null) {
            plantChunkStorage.removePlantBlock(block);
        }
        // if plant block has a parent, remove from parent's children list
        if (block.hasParent()) {
            PlantBlock parent = getPlantBlock(block.getParentLocation());
            if (parent != null) {
                parent.removeChildLocation(block.getLocation());
            }
        }
        // if plant block has a guardian, remove from guardian's children list
        if (block.hasGuardian()) {
            PlantBlock guardian = getPlantBlock(block.getGuardianLocation());
            if (guardian != null) {
                guardian.removeChildLocation(block.getLocation());
            }
        }
        // if block has children, remove self as parent/guardian of them
        for (BlockLocation location : block.getChildLocations()) {
            PlantBlock child = getPlantBlock(location);
            if (child != null) {
                child.removeParentOrGuardian(block.getLocation());
            }
        }
    }

    public void removePlantBlock(Block block) {
        // get plantBlock if exists at block location
        PlantBlock plantBlock = getPlantBlock(block);
        if (plantBlock != null) {
            removePlantBlock(plantBlock);
        }
    }

    public PlantChunk getPlantChunk(ChunkLocation chunkLocation) {
        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(chunkLocation);
        if (plantChunkStorage != null) {
            return plantChunkStorage.getPlantChunk(chunkLocation);
        }
        return null;
    }

    public PlantChunk getPlantChunk(Chunk chunk) {
        return getPlantChunk(new ChunkLocation(chunk));
    }

    public PlantBlock getPlantBlock(BlockLocation blockLocation) {
        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(blockLocation.getWorldName());
        if (plantChunkStorage != null) {
            return plantChunkStorage.getPlantBlock(blockLocation);
        }
        return null;
    }

    public PlantBlock getPlantBlock(Block block) {
        return getPlantBlock(new BlockLocation(block));
    }

    //region Task Control

    public void startGrowthTask(PlantBlock plantBlock) {
        plantBlock.startTask(main);
    }

    public void startGrowthTask(BlockLocation blockLocation) {
        PlantBlock plantBlock = getPlantBlock(blockLocation);
        if (plantBlock != null) {
            startGrowthTask(plantBlock);
        }
    }

    public void pauseGrowthTask(PlantBlock plantBlock) {
        plantBlock.pauseTask();
    }

    public void pauseGrowthTask(BlockLocation blockLocation) {
        PlantBlock plantBlock = getPlantBlock(blockLocation);
        if (plantBlock != null) {
            pauseGrowthTask(plantBlock);
        }
    }

    public void setGrowthTaskToStage(PlantBlock plantBlock, int stage) {
        plantBlock.setTaskStage(main, stage);
    }

    public void setGrowthTaskToStage(BlockLocation blockLocation, int stage) {
        PlantBlock plantBlock = getPlantBlock(blockLocation);
        if (plantBlock != null) {
            setGrowthTaskToStage(plantBlock, stage);
        }
    }

    //endregion

//    private void removePlantChunk(PlantChunk chunk) {
//        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(chunk.getLocation());
//        if (plantChunkStorage != null) {
//            plantChunkStorage.removePlantChunk(chunk);
//            main.getLogger().info("Removed PlantChunk: " + chunk.toString());
//        }
//    }

    private void addPlantChunkStorage(PlantChunkStorage plantChunkStorage) {
        plantChunkStorageMap.put(plantChunkStorage.getWorldName(), plantChunkStorage);
    }

}
