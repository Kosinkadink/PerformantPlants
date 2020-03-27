package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class PlantBlock {
    private final BlockLocation location;
    private BlockLocation parentLocation;
    private BlockLocation guardianLocation;
    private HashSet<BlockLocation> childLocations;
    private Plant plant;
    private int stage;
    private String stageBlockId;
    private boolean grows = false;
    private long taskStartTime;
    private long duration;
    private BukkitTask growthTask;
    private UUID playerUUID;
    private ArrayList<Drop> drops = new ArrayList<>();

    public PlantBlock(BlockLocation blockLocation, Plant plant) {
        location = blockLocation;
        this.plant = plant;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, boolean grows) {
        this(blockLocation, plant);
        this.grows = grows;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID) {
        this(blockLocation,plant);
        this.playerUUID = playerUUID;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID, boolean grows) {
        this(blockLocation, plant, playerUUID);
        this.grows = grows;
    }

    public BlockLocation getLocation() {
        return location;
    }

    public BlockLocation getParentLocation() {
        return parentLocation;
    }

    public BlockLocation getGuardianLocation() {
        return guardianLocation;
    }

    public void setParentLocation(BlockLocation blockLocation) {
        parentLocation = blockLocation;
    }

    public void setGuardianLocation(BlockLocation blockLocation) {
        guardianLocation = blockLocation;
    }

    public boolean hasParent() {
        return parentLocation != null;
    }

    public boolean hasGuardian() {
        return guardianLocation != null;
    }

    public void removeParentOrGuardian(BlockLocation blockLocation) {
        if (blockLocation == parentLocation) {
            parentLocation = null;
        }
        if (blockLocation == guardianLocation) {
            guardianLocation = null;
        }
    }

    public HashSet<BlockLocation> getChildLocations() {
        return childLocations;
    }

    public void addChildLocation(BlockLocation blockLocation) {
        childLocations.add(blockLocation);
    }

    public void removeChildLocation(BlockLocation blockLocation) {
        childLocations.remove(blockLocation);
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public Plant getPlant() {
        return plant;
    }

    public int getStage() {
        return stage;
    }

    public String getStageBlockId() {
        return stageBlockId;
    }

    public boolean getGrows() {
        return grows;
    }

    public long getDuration() {
        return duration;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ArrayList<Drop> getDrops() {
        if (!hasGrowthStages()) {
            return drops;
        } else {
            GrowthStage growthStage = plant.getGrowthStage(stage);
            if (growthStage != null) {
                GrowthStageBlock stageBlock = growthStage.getGrowthStageBlock(stageBlockId);
                // get stageBlock's drops if exist
                if (stageBlock != null && stageBlock.getDrops().size() > 0) {
                    return stageBlock.getDrops();
                }
                // otherwise use growthStage's drops
                return growthStage.getDrops();
            }
        }
        return new ArrayList<>();
    }

    public boolean hasGrowthStages() {
        return plant.getTotalGrowthStages() > 0;
    }

    //region Task Control

    public void startTask(Main main) {
        // if plantBlock doesn't grow, then do nothing
        if (!grows || growthTask != null) {
            return;
        }
        // set new growth start time
        taskStartTime = System.currentTimeMillis();
        // start task for current growth stage
        growthTask = Bukkit.getScheduler().runTaskLater(main, () -> performGrowth(main), duration);
    }

    public void pauseTask() {
        // try to cancel task
        if (growthTask != null) {
            growthTask.cancel();
            // figure out remaining time
            long millisPassed = System.currentTimeMillis()-taskStartTime;
            // subtract ticks passed from duration
            duration -= TimeHelper.millisToTicks(millisPassed);
            if (duration < 0) {
                duration = 0;
            }
            // set task to null
            growthTask = null;
        }
    }

    public void setTaskStage(Main main, int growthStage) {
        // set plantBlock's growth stage, resetting any growth task it currently has
        pauseTask();
        stage = growthStage;
        // TODO: set duration to valid length for stage
        duration = 0;
        startTask(main);
    }

    public void performGrowth(Main main) {
        // if plant metadata for block is not set, don't do anything and stop task
        if (!MetadataHelper.hasPlantBlockMetadata(getBlock())) {
            return;
        }
        // TODO: do what's required of growth stage
        // increment growth stage
        stage++;
        // set new growth start time
        taskStartTime = System.currentTimeMillis();
        // queue up new task
        growthTask = Bukkit.getScheduler().runTaskLater(main, () -> performGrowth(main), duration);
    }

    //endregion

    @Override
    public String toString() {
        return "PlantBlock @ " + location.toString();
    }

}
