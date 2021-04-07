package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
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

    private final PerformantPlants performantPlants;
    private static final HashMap<String, PlantChunkStorage> plantChunkStorageMap = new HashMap<>();

    public PlantManager(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
        createPlantChunkStorageMap();
    }

    public void createPlantChunkStorageMap() {
        for (World world : Bukkit.getWorlds()) {
            PlantChunkStorage plantChunkStorage = new PlantChunkStorage(performantPlants, world.getName());
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
            plantChunkStorage = new PlantChunkStorage(performantPlants, chunkLocation.getWorldName());
            addPlantChunkStorage(plantChunkStorage);
        }
        plantChunkStorage.addPlantBlock(block);
        // start growth task
        startGrowthTask(block);
    }

    public boolean removePlantBlock(PlantBlock block) {
        // pause growth task
        pauseGrowthTask(block);
        // get plant chunk
        PlantChunkStorage plantChunkStorage = getPlantChunkStorage(block.getLocation().getWorldName());
        if (plantChunkStorage != null) {
            boolean removed = plantChunkStorage.removePlantBlock(block);
            if (!removed) {
                return false;
            }
        } else {
            return false;
        }
        // if plant block has a parent, remove from parent's children list
        if (block.hasParent()) {
            PlantBlock parent = getPlantBlock(block.getParentLocation());
            if (parent != null) {
                parent.removeChildLocation(block.getLocation());
                // if parent's stage should be updated, do it
                if (block.isUpdateStageOnBreak()) {
                    parent.goToPreviousStageGracefully(performantPlants, block.getStageIndex());
                }
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
        return true;
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
        plantBlock.startTask(performantPlants);
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

    public void setGrowthTaskStage(PlantBlock plantBlock, int stage) {
        plantBlock.goToPreviousStageGracefully(performantPlants, stage);
    }

    public void setGrowthTaskStage(BlockLocation blockLocation, int stage) {
        PlantBlock plantBlock = getPlantBlock(blockLocation);
        if (plantBlock != null) {
            setGrowthTaskStage(plantBlock, stage);
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
