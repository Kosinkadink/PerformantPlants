package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.interfaces.Droppable;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlantBlock implements Droppable {
    private final BlockLocation location;
    private BlockLocation parentLocation;
    private BlockLocation guardianLocation;
    private HashSet<BlockLocation> childLocations = new HashSet<>();
    private Plant plant;
    private int stageIndex;
    private int dropStageIndex;
    private boolean executedStage = false;
    private float blockYaw;
    private String stageBlockId;
    private boolean grows;
    private long taskStartTime;
    private long duration;
    private BukkitTask growthTask;
    private UUID playerUUID;
    private UUID plantUUID;
    private ArrayList<Drop> drops = new ArrayList<>();

    public PlantBlock(BlockLocation blockLocation, Plant plant, boolean grows) {
        location = blockLocation;
        this.plant = plant;
        this.grows = grows;
        this.plantUUID = UUID.randomUUID();
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, boolean grows, UUID plantUUID) {
        location = blockLocation;
        this.plant = plant;
        this.grows = grows;
        this.plantUUID = plantUUID;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID, boolean grows) {
        this(blockLocation, plant, grows);
        this.playerUUID = playerUUID;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID, boolean grows, UUID plantUUID) {
        this(blockLocation, plant, grows, plantUUID);
        this.playerUUID = playerUUID;
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

    public int getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(int index) {
        if (index >= 0) {
            stageIndex = index;
        }
    }

    public int getDropStageIndex() {
        return dropStageIndex;
    }

    public void setDropStageIndex(int index) {
        if (index >= 0) {
            dropStageIndex = index;
        }
    }

    public boolean isExecutedStage() {
        return executedStage;
    }

    public void setExecutedStage(boolean executedStage) {
        this.executedStage = executedStage;
    }

    public String getStageBlockId() {
        return stageBlockId;
    }

    public void setStageBlockId(String id) {
        stageBlockId = id;
    }

    public boolean getGrows() {
        return grows;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long dur) {
        if (dur > 0) {
            duration = dur;
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getPlantUUID() {
        return plantUUID;
    }

    public int getDropLimit() {
        if (plant.hasGrowthStages()) {
            GrowthStage growthStage = plant.getGrowthStage(dropStageIndex);
            if (growthStage != null) {
                return growthStage.getDropLimit(stageBlockId);
            }
        }
        return 0;
    }

    @Override
    public void setDropLimit(int limit) {
        // do nothing
    }

    public ArrayList<Drop> getDrops() {
        if (!plant.hasGrowthStages()) {
            return drops;
        } else {
            GrowthStage growthStage = plant.getGrowthStage(dropStageIndex);
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

    @Override
    public void addDrop(Drop drop) {
        // do nothing
    }

    public boolean isBreakChildren() {
        if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock growthStageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (growthStageBlock != null) {
                return growthStageBlock.isBreakChildren();
            }
        }
        return false;
    }

    public boolean isBreakParent() {
        if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock growthStageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (growthStageBlock != null) {
                return growthStageBlock.isBreakParent();
            }
        }
        return false;
    }

    public boolean isUpdateStageOnBreak() {
        if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock growthStageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (growthStageBlock != null) {
                return growthStageBlock.isUpdateStageOnBreak();
            }
        }
        return false;
    }

    public boolean isStopGrowth() {
        if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock growthStageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (growthStageBlock != null) {
                return growthStageBlock.isStopGrowth();
            }
        }
        return false;
    }

    public PlantInteract getOnInteract(ItemStack itemStack) {
        if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock growthStageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (growthStageBlock != null) {
                return growthStageBlock.getOnInteract(itemStack);
            }
        }
        return null;
    }

    public float getBlockYaw() {
        return blockYaw;
    }

    public void setBlockYaw(float blockYaw) {
        this.blockYaw = blockYaw;
    }

    //region Task Control

    public void startTask(Main main) {
        // if plantBlock doesn't grow or is already growing, then do nothing
        if (!grows || growthTask != null || !plant.hasGrowthStages()) {
            return;
        }
        // check if plant is done growing (if invalid, done growing)
        if (!plant.isValidStage(stageIndex)) {
            return;
        }

        // set new growth start time
        taskStartTime = System.currentTimeMillis();
        // start task for current growth stage
        growthTask = main.getServer().getScheduler().runTaskLater(main, () -> performGrowth(main, true), duration);
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

    public void goToPreviousStageGracefully(Main main, int growthStageIndex) {
        // check if proposed growth stage is valid; if not, do nothing
        if (!plant.isValidStage(growthStageIndex)) {
            main.getLogger().info("Could not setTaskStage for block " + toString() + "; stage is invalid: "
                    + growthStageIndex);
            return;
        }
        // if trying to go forward a stage, do nothing
        if (growthStageIndex > stageIndex) {
            return;
        }
        // if same stage and already growing, do nothing (would needlessly reset duration)
        else if (growthStageIndex == stageIndex && grows) {
            return;
        }
        // if trying to go forward a stage,
        main.getLogger().info("Changing stage to " + growthStageIndex + " from " + stageIndex + " for block " + toString());
        // set plantBlock's growth stage, resetting any growth task it currently has
        pauseTask();
        stageIndex = growthStageIndex;
        // set duration to valid length for stage
        duration = plant.generateGrowthTime(stageIndex);
        // set grows to true
        // set execute to false
        grows = true;
        executedStage = false;
        startTask(main);
    }

    public boolean goToNextStage(Main main) {
        main.getLogger().info("goToNextStage fired for block: " + toString());
        // perform growth without advancement
        boolean canGrow = performGrowth(main, false);
        // if couldn't grow, do nothing
        if (!canGrow) {
            return false;
        }
        // otherwise, pause task
        pauseTask();
        // advance to next stage (or finish growing, if applicable)
        main.getLogger().info("goToNextStage advancing stage for block: " + toString());
        advanceStage(main, true, true);
        return true;
    }

    public boolean goToStageForcefully(Main main, int growthStageIndex) {
        main.getLogger().info(String.format("goToStageForcefully (stage %d) fired for block: %s",growthStageIndex,toString()));
        // pause task
        pauseTask();
        // check that growthsStageIndex is valid
        if (!plant.isValidStage(growthStageIndex)) {
            startTask(main);
            return false;
        }
        // perform growth without advancement
        boolean canGrow = performGrowth(main, false);
        // if couldn't grow, do nothing (grow naturally)
        if (!canGrow) {
            startTask(main);
            return false;
        }
        // otherwise, pause task
        int previousStageIndex = stageIndex;
        stageIndex = growthStageIndex;
        executedStage = false;
        // perform growth again without advancement to get block to this stage
        canGrow = performGrowth(main, false);
        // if can't grow, revert back to previous state and continue previous task
        if (!canGrow) {
            stageIndex = previousStageIndex;
            executedStage = true;
            startTask(main);
            return false;
        }
        // otherwise, advance to next stage
        grows = true;
        main.getLogger().info(String.format("goToStageForcefully advancing stage (from %d to %d for block: %s)",previousStageIndex, stageIndex, toString()));
        advanceStage(main, true);
        return true;
    }

    public boolean performGrowth(Main main, boolean advance) {
        // if plant metadata for block is not set, remove self and stop task
        if (!MetadataHelper.hasPlantBlockMetadata(getBlock())) {
            main.getPlantManager().removePlantBlock(this);
            return false;
        }
        // check if requirements are met for stage growth
        boolean canGrow = checkAllRequirements(main);
        // if requirements are met, perform growth actions
        if (canGrow && !executedStage) {
            Block thisBlock = getBlock();
            HashMap<GrowthStageBlock,PlantBlock> blocksWithGuardiansAdded = new HashMap<>();
            for (GrowthStageBlock growthStageBlock : plant.getGrowthStage(stageIndex).getBlocks().values()) {
                // if keep block vanilla, just change blockData
                Block block = BlockHelper.getAbsoluteBlock(main, thisBlock, growthStageBlock.getLocation());
                // if not air or not relevant plant block, continue
                if (!block.isEmpty() && !MetadataHelper.hasPlantBlockMetadata(block, plantUUID)) {
                    continue;
                }
                // if at current location, update values of this PlantBlock; don't create a new one
                if (growthStageBlock.getLocation().equals(new RelativeLocation(0, 0, 0))) {
                    // update block data at location
                    BlockHelper.setBlockData(block, growthStageBlock, this);
                    // set drop stage index
                    setDropStageIndex(stageIndex);
                    // set growth stage block
                    setStageBlockId(growthStageBlock.getId());
                } else {
                    // update block data at location without plantBlock
                    BlockHelper.setBlockData(block, growthStageBlock, null);
                    // create plant blocks at location
                    PlantBlock newPlantBlock;
                    newPlantBlock = new PlantBlock(new BlockLocation(block), plant, playerUUID, false,
                            plantUUID);
                    // set stage index and stage block id
                    newPlantBlock.setStageIndex(stageIndex);
                    newPlantBlock.setDropStageIndex(stageIndex);
                    newPlantBlock.setStageBlockId(growthStageBlock.getId());
                    // set parent to be this block
                    newPlantBlock.setParentLocation(location);
                    // add block as child
                    addChildLocation(newPlantBlock.getLocation());
                    // add plant block via plantManager
                    main.getPlantManager().addPlantBlock(newPlantBlock);
                    // if has childOf set, add to blocksAdded
                    if (growthStageBlock.hasChildOf()) {
                        blocksWithGuardiansAdded.put(growthStageBlock, newPlantBlock);
                    }
                }
            }
            // add children/guardians to any blocks that require them
            for (Map.Entry<GrowthStageBlock,PlantBlock> entry : blocksWithGuardiansAdded.entrySet()) {
                GrowthStageBlock growthStageBlock = entry.getKey();
                PlantBlock newPlantBlock = entry.getValue();
                // get guardian location
                BlockLocation guardianLocation = location.getLocationFromRelative(growthStageBlock.getChildOf());
                // add guardian to plant block
                newPlantBlock.setGuardianLocation(guardianLocation);
                // get guardian block
                PlantBlock guardianBlock = main.getPlantManager().getPlantBlock(guardianLocation);
                // add child to guardian block
                guardianBlock.addChildLocation(newPlantBlock.getLocation());
            }
            // perform on-execute
            if (!executedStage) {
                PlantInteract onExecute = plant.getGrowthStage(dropStageIndex).getOnExecute();
                if (onExecute != null) {
                    DropHelper.performDrops(onExecute.getDropStorage(), getBlock());
                    // perform any effects set
                    onExecute.getEffectStorage().performEffects(getBlock());
                }
            }
            executedStage = true;
        }
        if (advance) {
            advanceStage(main, canGrow);
        }
        return canGrow;
    }

    void advanceStage(Main main, boolean canGrow, boolean interacted) {
        // see if current stage is a stopping point
        boolean growthCheckpoint = plant.isGrowthCheckpoint(stageIndex) && !interacted;
        boolean forcedToGrow = !grows && interacted;
        if (forcedToGrow) {
            grows = true;
        }
        PlantInteract onExecute = plant.getGrowthStage(dropStageIndex).getOnExecute();
        if (onExecute != null) {
            // go to stage, if set
            if (onExecute.getGoToStage() != null) {
                if (plant.getStageStorage().isValidStage(onExecute.getGoToStage())) {
                    goToStageForcefully(main, plant.getStageStorage().getGrowthStageIndex(onExecute.getGoToStage()));
                    return;
                }
            }
        }
        if (canGrow && !growthCheckpoint && grows) {
            // increment growth stage; if would not be valid, stop growing
            if (plant.isValidStage(stageIndex + 1)) {
                stageIndex++;
                executedStage = false;
            } else {
                grows = false;
                duration = 0;
            }
        }
        // queue new task only if block is still growing
        if (grows && !growthCheckpoint) {
            // set new growth start time
            taskStartTime = System.currentTimeMillis();
            // set new growth duration
            if (forcedToGrow) {
                duration = 0;
            } else {
                duration = plant.generateGrowthTime(stageIndex);
            }
            // queue up new task
            growthTask = main.getServer().getScheduler().runTaskLater(main, () -> performGrowth(main, true), duration);
            main.getLogger().info("Growth Task queued up for " + toString()
                    + " in " + duration + " ticks; at stage " + stageIndex);
        } else {
            main.getLogger().info("Plant can't grow any further for block type '"
                    + plant.getId() + "' at stage: " + stageIndex);
            // stop growth if failure to grow caused by checkpoint
            if (growthCheckpoint) {
                grows = false;
            }
        }
    }

    void advanceStage(Main main, boolean canGrow) {
        advanceStage(main, canGrow, false);
    }

    //endregion

    //region Requirements Check

    public boolean checkAllRequirements(Main main) {
        // check that grow block requirements are met
        if (!checkGrowthRequirements(main)) {
            return false;
        }
        // check that space is available for blocks that are required to be placed
        if (!checkSpaceRequirements(main)) {
            main.getLogger().info("No space to grow for " + toString());
            return false;
        }
        return true;
    }

    public boolean checkGrowthRequirements(Main main) {
        if (plant.isWaterRequired()) {
            if (!checkWaterPresent(main)) {
                return false;
            }
        }
        if (plant.isLavaRequired()) {
            if (!checkLavaPresent(main)) {
                return false;
            }
        }
        // check if required blocks are present
        if (plant.hasRequiredBlocksToGrow()) {
            boolean enoughMatch = false;
            Block thisBlock = getBlock();
            for (RequiredBlock requiredBlock : plant.getRequiredBlocksToGrow()) {
                Block block = BlockHelper.getAbsoluteBlock(main, thisBlock, requiredBlock.getLocation());
                if (requiredBlock.checkIfMatches(block)) {
                    enoughMatch = true;
                } else if (requiredBlock.isRequired()) {
                    return false;
                }
            }
            return enoughMatch;
        }
        return true;
    }

    boolean checkSpaceRequirements(Main main) {
        // check that growth stage blocks can be placed, if required
        Block thisBlock = getBlock();
        HashMap<String,GrowthStageBlock> blocks = plant.getGrowthStage(stageIndex).getBlocks();
        for (GrowthStageBlock growthStageBlock : blocks.values()) {
            if (!growthStageBlock.isIgnoreSpace()) {
                Block block = BlockHelper.getAbsoluteBlock(main, thisBlock, growthStageBlock.getLocation());
                if (!block.isEmpty() && !MetadataHelper.hasPlantBlockMetadata(block, plantUUID)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkWaterPresent(Main main) {
        // check if there is a water or water-logged block adjacent
        ArrayList<Block> blocksToCheck = new ArrayList<>();
        Block thisBlock = getBlock();
        // check 1 block to the west
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(-1,-1,0)));
        // check 1 block to the east
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(1,-1,0)));
        // check 1 block to the north
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(0,-1,-1)));
        // check 1 block to the south
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(0,-1,1)));
        for (Block block : blocksToCheck) {
            if (BlockHelper.hasWater(block)) {
                return true;
            }
        }
        return false;
    }

    boolean checkLavaPresent(Main main) {
        // check if there is a water or water-logged block adjacent
        ArrayList<Block> blocksToCheck = new ArrayList<>();
        Block thisBlock = getBlock();
        // check 1 block to the west
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(-1,-1,0)));
        // check 1 block to the east
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(1,-1,0)));
        // check 1 block to the north
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(0,-1,-1)));
        // check 1 block to the south
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(main, thisBlock, new RelativeLocation(0,-1,1)));
        for (Block block : blocksToCheck) {
            // if any lava found, return true
            if (block.getType() == Material.LAVA) {
                return true;
            }
        }
        // otherwise return false
        return false;
    }

    //endregion

    @Override
    public String toString() {
        return String.format("PlantBlock (%s) @ %s", plant.getId(), location.toString());
    }

}
