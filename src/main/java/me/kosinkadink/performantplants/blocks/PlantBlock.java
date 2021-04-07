package me.kosinkadink.performantplants.blocks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.locations.RelativeLocation;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.PlantData;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.stages.GrowthStage;
import me.kosinkadink.performantplants.storage.RequirementStorage;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.TimeHelper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlantBlock {
    private final BlockLocation location;
    private BlockLocation parentLocation;
    private BlockLocation guardianLocation;
    private final ArrayList<BlockLocation> anchorLocations = new ArrayList<>();
    private final HashSet<BlockLocation> childLocations = new HashSet<>();
    private final Plant plant;
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
    private final UUID plantUUID;
    private PlantData plantData = null;
    // temporary state variables
    private boolean isNewlyPlaced = false;
    private boolean destroyBehaviorExecuted = false;
    private BlockFace direction = null;
    // cached values
    private Block block = null;

    public PlantBlock(BlockLocation blockLocation, Plant plant, boolean grows) {
        location = blockLocation;
        this.plant = plant;
        this.grows = grows;
        this.plantUUID = UUID.randomUUID();
        initializePlantData();
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, boolean grows, UUID plantUUID) {
        location = blockLocation;
        this.plant = plant;
        this.grows = grows;
        this.plantUUID = plantUUID;
        initializePlantData();
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID, boolean grows) {
        this(blockLocation, plant, grows);
        this.playerUUID = playerUUID;
    }

    public PlantBlock(BlockLocation blockLocation, Plant plant, UUID playerUUID, boolean grows, UUID plantUUID) {
        this(blockLocation, plant, grows, plantUUID);
        this.playerUUID = playerUUID;
    }

    public void forcefullyInitializePlantData() {
        if (plant != null && plant.hasPlantData()) {
            plantData = plant.getPlantData().clone();
        }
    }

    void initializePlantData() {
        if (grows) {
            forcefullyInitializePlantData();
        }
    }

    public boolean isNewlyPlaced() {
        return isNewlyPlaced;
    }

    public void setNewlyPlaced(boolean newlyPlaced) {
        isNewlyPlaced = newlyPlaced;
    }

    public boolean isDestroyBehaviorExecuted() {
        return destroyBehaviorExecuted;
    }

    public void setDestroyBehaviorExecuted(boolean destroyBehaviorExecuted) {
        this.destroyBehaviorExecuted = destroyBehaviorExecuted;
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

    public boolean hasAnchors() {
        return !anchorLocations.isEmpty();
    }

    public ArrayList<BlockLocation> getAnchorLocations() {
        return anchorLocations;
    }

    public void addAnchorLocation(BlockLocation anchorLocation) {
        anchorLocations.add(anchorLocation);
    }

    public Block getBlock() {
        if (block != null) {
            return block;
        }
        block = location.getBlock();
        return block;
    }

    public BlockLocation getEffectiveLocation() {
        if (hasParent()) {
            return getParentLocation();
        }
        return getLocation();
    }

    public Block getEffectiveBlock() {
        if (hasParent()) {
            return getParentLocation().getBlock();
        }
        return getBlock();
    }

    public PlantBlock getEffectivePlantBlock() {
        if (hasParent()) {
            return PerformantPlants.getInstance().getPlantManager().getPlantBlock(getParentLocation());
        }
        return this;
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

    public boolean isGrows() {
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

    public boolean isBreakChildren() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isBreakChildren();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isBreakChildren();
            }
        }
        return false;
    }

    public boolean isBreakParent() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isBreakParent();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isBreakParent();
            }
        }
        return false;
    }

    public boolean isUpdateStageOnBreak() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isUpdateStageOnBreak();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isUpdateStageOnBreak();
            }
        }
        return false;
    }

    public boolean isStopGrowth() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isStopGrowth();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.isStopGrowth();
            }
        }
        return false;
    }

    public ScriptBlock getOnInteract() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnRightClick();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnRightClick();
            }
        }
        return null;
    }

    public ScriptBlock getOnClick() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnLeftClick();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnLeftClick();
            }
        }
        return null;
    }

    public ScriptBlock getOnBreak() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnBreak();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnBreak();
            }
        }
        return null;
    }

    public ScriptBlock getOnExplode() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnExplode();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnExplode();
            }
        }
        return null;
    }

    public ScriptBlock getOnBurn() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnBurn();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnBurn();
            }
        }
        return null;
    }

    public ScriptBlock getOnPiston() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnPiston();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnPiston();
            }
        }
        return null;
    }

    public ScriptBlock getOnFade() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                ScriptBlock toReturn = stageBlock.getOnFade();
                if (toReturn == null && !stageBlock.isPassFade()) {
                    return ScriptResult.FALSE;
                }
                return toReturn;
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                ScriptBlock toReturn = stageBlock.getOnFade();
                if (toReturn == null && !stageBlock.isPassFade()) {
                    return ScriptResult.FALSE;
                }
                return toReturn;
            }
        }
        return null;
    }

    public ScriptBlock getOnDecay() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                ScriptBlock toReturn = stageBlock.getOnDecay();
                if (toReturn == null && !stageBlock.isPassDecay()) {
                    return ScriptResult.FALSE;
                }
                return toReturn;
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                ScriptBlock toReturn = stageBlock.getOnDecay();
                if (toReturn == null && !stageBlock.isPassDecay()) {
                    return ScriptResult.FALSE;
                }
                return toReturn;
            }
        }
        return null;
    }

    public ScriptBlock getOnDestroy() {
        if (dropStageIndex == -1) {
            GrowthStageBlock stageBlock = plant.getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnDestroy();
            }
        }
        else if (plant.hasGrowthStages() && plant.isValidStage(dropStageIndex)) {
            GrowthStageBlock stageBlock = plant.getGrowthStage(dropStageIndex).getGrowthStageBlock(stageBlockId);
            if (stageBlock != null) {
                return stageBlock.getOnDestroy();
            }
        }
        return null;
    }

    public float getBlockYaw() {
        return blockYaw;
    }

    public void setBlockYaw(float blockYaw) {
        this.blockYaw = blockYaw;
        direction = null;
    }

    public BlockFace getDirection() {
        if (direction == null && plant.isRotatePlant()) {
            direction = BlockHelper.getDirectionFromYaw(this.getBlockYaw());
        }
        return direction;
    }

    public boolean hasPlantData() {
        return plantData != null;
    }

    public PlantData getPlantData() {
        return plantData;
    }

    public PlantData getEffectivePlantData() {
        if (hasParent()) {
            PlantBlock parent = PerformantPlants.getInstance().getPlantManager().getPlantBlock(getParentLocation());
            if (parent != null) {
                return parent.getPlantData();
            }
        }
        return getPlantData();
    }

    public void setPlantData(PlantData plantData) {
        this.plantData = plantData;
    }

    //region Task Control

    public void startTask(PerformantPlants performantPlants) {
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
        growthTask = performantPlants.getServer().getScheduler().runTaskLater(performantPlants, () -> performGrowth(performantPlants, true), duration);
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

    public void goToPreviousStageGracefully(PerformantPlants performantPlants, int growthStageIndex) {
        // check if proposed growth stage is valid; if not, do nothing
        if (!plant.isValidStage(growthStageIndex)) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Could not setTaskStage for block " + toString() + "; stage is invalid: "
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
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Changing stage to " + growthStageIndex + " from " + stageIndex + " for block " + toString());
        // set plantBlock's growth stage, resetting any growth task it currently has
        pauseTask();
        stageIndex = growthStageIndex;
        // set duration to valid length for stage
        duration = plant.generateGrowthTime(stageIndex, new ExecutionContext().set(this));
        // set grows to true
        // set execute to false
        grows = true;
        executedStage = false;
        startTask(performantPlants);
    }

    public boolean goToNextStage(PerformantPlants performantPlants) {
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("goToNextStage fired for block: " + toString());
        // perform growth without advancement
        boolean canGrow = performGrowth(performantPlants, false);
        // if couldn't grow, do nothing
        if (!canGrow) {
            return false;
        }
        // otherwise, pause task
        pauseTask();
        // advance to next stage (or finish growing, if applicable)
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("goToNextStage advancing stage for block: " + toString());
        advanceStage(performantPlants, true, true);
        return true;
    }

    public boolean goToStageForcefully(PerformantPlants performantPlants, int growthStageIndex) {
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info(String.format("goToStageForcefully (stage %d) fired for block: %s",growthStageIndex,toString()));
        // if stage index not valid, do nothing
        if (!plant.isValidStage(growthStageIndex)) {
            return false;
        }
        // pause task
        pauseTask();
        // remember old stage index
        int previousStageIndex = stageIndex;
        stageIndex = growthStageIndex;
        executedStage = false;
        // perform growth again without advancement to get block to this stage
        boolean canGrow = performGrowth(performantPlants, false);
        // if can't grow, revert back to previous state and continue previous task
        if (!canGrow) {
            stageIndex = previousStageIndex;
            executedStage = true;
            startTask(performantPlants);
            return false;
        }
        // otherwise, advance to next stage
        grows = true;
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info(String.format("goToStageForcefully advancing stage (from %d to %d for block: %s)",previousStageIndex, stageIndex, toString()));
        advanceStage(performantPlants, true);
        return true;
    }

    public boolean performGrowth(PerformantPlants performantPlants, boolean advance) {
        // if plant metadata for block is not set, remove self and stop task
        if (!MetadataHelper.hasPlantBlockMetadata(getBlock())) {
            performantPlants.getPlantManager().removePlantBlock(this);
            return false;
        }
        // check if requirements are met for stage growth
        boolean canGrow = checkAllRequirements(performantPlants);
        if (isNewlyPlaced && !canGrow) {
            grows = false;
        }
        // if requirements are met, perform growth actions
        if (canGrow && !executedStage) {
            // add anchors
            if (isNewlyPlaced) {
                if (plant.isUseClickedAsAnchor()) {
                    // anchor to use should already be in local anchorLocations list; register it
                    for (BlockLocation anchorLocation : anchorLocations) {
                        performantPlants.getAnchorManager().addAnchorBlock(anchorLocation, location);
                    }
                }
                if (plant.hasAnchors()){
                    for (RelativeLocation relativeLocation : plant.getAnchorLocations()) {
                        Block block = BlockHelper.getAbsoluteBlock(getBlock(), relativeLocation, this, this.getDirection());
                        if (block.isEmpty()) {
                            continue;
                        }
                        BlockLocation anchorLocation = new BlockLocation(block);
                        addAnchorLocation(anchorLocation);
                        performantPlants.getAnchorManager().addAnchorBlock(anchorLocation, location);
                    }
                }
            }
            setNewlyPlaced(false);
            Block thisBlock = getBlock();
            HashMap<GrowthStageBlock,PlantBlock> blocksWithGuardiansAdded = new HashMap<>();
            for (GrowthStageBlock growthStageBlock : plant.getGrowthStage(stageIndex).getBlocks().values()) {
                // if keep block vanilla, just change blockData
                Block block = BlockHelper.getAbsoluteBlock(thisBlock, growthStageBlock.getLocation(), this, this.getDirection());
                // if not air or not relevant plant block, continue
                if (!block.isEmpty() && !MetadataHelper.hasPlantBlockMetadata(block, plantUUID)) {
                    continue;
                }
                // if at current location, update values of this PlantBlock; don't create a new one
                if (growthStageBlock.getLocation().equals(new RelativeLocation(0, 0, 0))) {
                    // update block data at location
                    BlockHelper.setBlockData(block, growthStageBlock, this, this.getDirection());
                    // set drop stage index
                    setDropStageIndex(stageIndex);
                    // set growth stage block
                    setStageBlockId(growthStageBlock.getId());
                } else {
                    // update block data at location without plantBlock
                    BlockHelper.setBlockData(block, growthStageBlock, null, this.getDirection());
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
                    performantPlants.getPlantManager().addPlantBlock(newPlantBlock);
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
                PlantBlock guardianBlock = performantPlants.getPlantManager().getPlantBlock(guardianLocation);
                // add child to guardian block
                guardianBlock.addChildLocation(newPlantBlock.getLocation());
            }
            // perform on-execute
            if (!executedStage) {
                executedStage = true;
                ScriptBlock onExecute = plant.getGrowthStage(dropStageIndex).getOnExecute();
                if (onExecute != null) {
                    ExecutionContext context = new ExecutionContext().set(this);
                    int currentStageIndex = stageIndex;
                    onExecute.loadValue(context).getBooleanValue();
                    // make sure no advancement happens if script block causes growth stage change
                    if (currentStageIndex != stageIndex) {
                        advance = false;
                    }
                }
            }
        }
        if (!canGrow) {
            ScriptBlock onFail = plant.getGrowthStage(dropStageIndex).getOnFail();
            if (onFail != null) {
                ExecutionContext context = new ExecutionContext().set(this);
                int currentStageIndex = stageIndex;
                onFail.loadValue(context);
                // make sure no advancement happens if script block causes growth stage change
                if (currentStageIndex != stageIndex) {
                    advance = false;
                }
            }
        }
        if (advance) {
            advanceStage(performantPlants, canGrow);
        }
        return canGrow;
    }

    void advanceStage(PerformantPlants performantPlants, boolean canGrow, boolean interacted) {
        // see if current stage is a stopping point
        boolean growthCheckpoint = plant.isGrowthCheckpoint(stageIndex) && !interacted;
        boolean forcedToGrow = !grows && interacted;
        if (forcedToGrow) {
            grows = true;
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
                duration = plant.generateGrowthTime(stageIndex, new ExecutionContext().set(this));
            }
            // queue up new task
            growthTask = performantPlants.getServer().getScheduler().runTaskLater(performantPlants, () -> performGrowth(performantPlants, true), duration);
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Growth Task queued up for " + toString()
                    + " in " + duration + " ticks; at stage " + stageIndex);
        } else {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Plant can't grow any further for block type '"
                    + plant.getId() + "' at stage: " + stageIndex);
            // stop growth if failure to grow caused by checkpoint
            if (growthCheckpoint) {
                grows = false;
            }
        }
    }

    void advanceStage(PerformantPlants performantPlants, boolean canGrow) {
        advanceStage(performantPlants, canGrow, false);
    }

    //endregion

    //region Requirements Check

    public boolean checkAllRequirements(PerformantPlants performantPlants) {
        // check that grow block requirements are met
        if (!checkGrowthRequirements()) {
            return false;
        }
        // check that space is available for blocks that are required to be placed
        if (!checkSpaceRequirements()) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("No space to grow for " + toString());
            return false;
        }
        return true;
    }

    public boolean checkGrowthRequirements() {
        RequirementStorage requirements = plant.getGrowthRequirementStorage();
        if (isNewlyPlaced() && plant.hasPlantRequirements()) {
            requirements = plant.getPlantRequirementStorage();
        } else {
            if (plant.hasGrowthStages()) {
                GrowthStage growthStage = plant.getGrowthStage(stageIndex);
                if (growthStage != null && growthStage.getRequirementStorage().isSet()) {
                    requirements = growthStage.getRequirementStorage();
                }
            }
        }

        if (requirements.isSet()) {
            Block thisBlock = getBlock();
            if (requirements.hasWaterRequirement() && requirements.isWaterRequired()) {
                if (!checkWaterPresent(thisBlock)) {
                    return false;
                }
            }
            if (requirements.hasLavaRequirement() && requirements.isLavaRequired()) {
                if (!checkLavaPresent(thisBlock)) {
                    return false;
                }
            }
            if (requirements.hasLightRequirement()) {
                if (!checkLightPresent(thisBlock, requirements)) {
                    return false;
                }
            }
            if (requirements.hasTimeRequirement()) {
                if (!checkTimePresent(thisBlock, requirements)) {
                    return false;
                }
            }
            if (requirements.hasTemperatureRequirement()) {
                if (!checkTemperaturePresent(thisBlock, requirements)) {
                    return false;
                }
            }
            if (requirements.hasWorldRequirement()) {
                if (!checkWorldPresent(thisBlock, requirements)) {
                    return false;
                }
            }
            if (requirements.hasBiomeRequirement()) {
                if (!checkBiomePresent(thisBlock, requirements)) {
                    return false;
                }
            }
            if (requirements.hasEnvironmentRequirement()) {
                if (!checkEnvironmentPresent(thisBlock, requirements)) {
                    return false;
                }
            }
            // check if required blocks are present
            if (requirements.hasRequiredBlocks()) {
                boolean enoughMatch = false;
                for (RequiredBlock requiredBlock : requirements.getRequiredBlocks()) {
                    Block block = BlockHelper.getAbsoluteBlock(thisBlock, requiredBlock.getLocation(), this, this.getDirection());
                    if (requiredBlock.checkIfMatches(block)) {
                        enoughMatch = true;
                    } else if (requiredBlock.isCritical()) {
                        return false;
                    }
                }
                return enoughMatch;
            }
        }
        return true;
    }

    boolean checkSpaceRequirements() {
        // check that growth stage blocks can be placed, if required
        Block thisBlock = getBlock();
        HashMap<String,GrowthStageBlock> blocks = plant.getGrowthStage(stageIndex).getBlocks();
        for (GrowthStageBlock growthStageBlock : blocks.values()) {
            if (!growthStageBlock.isIgnoreSpace()) {
                Block block = BlockHelper.getAbsoluteBlock(thisBlock, growthStageBlock.getLocation(), this, this.getDirection());
                if (!block.isEmpty() && !MetadataHelper.hasPlantBlockMetadata(block, plantUUID)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkWaterPresent(Block thisBlock) {
        // check if there is a water or water-logged block adjacent
        ArrayList<Block> blocksToCheck = new ArrayList<>();
        // check 1 block to the west
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(-1,-1,0), null, null));
        // check 1 block to the east
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(1,-1,0), null, null));
        // check 1 block to the north
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,-1), null, null));
        // check 1 block to the south
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,1), null, null));
        for (Block block : blocksToCheck) {
            if (BlockHelper.hasWater(block)) {
                return true;
            }
        }
        return false;
    }

    boolean checkLavaPresent(Block thisBlock) {
        // check if there is a water or water-logged block adjacent
        ArrayList<Block> blocksToCheck = new ArrayList<>();
        // check 1 block to the west
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(-1,-1,0), null, null));
        // check 1 block to the east
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(1,-1,0), null, null));
        // check 1 block to the north
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,-1), null, null));
        // check 1 block to the south
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,1), null, null));
        for (Block block : blocksToCheck) {
            // if any lava found, return true
            if (block.getType() == Material.LAVA) {
                return true;
            }
        }
        // otherwise return false
        return false;
    }

    boolean checkLightPresent(Block thisBlock, RequirementStorage storage) {
        // check if there are any blocks nearby with required light levels
        ArrayList<Block> blocksToCheck = new ArrayList<>();
        // check 1 block up
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,1,0), null, null));
        // check 1 block to the west
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(-1,-1,0), null, null));
        // check 1 block to the east
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(1,-1,0), null, null));
        // check 1 block to the north
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,-1), null, null));
        // check 1 block to the south
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,1), null, null));
        // check 1 block down
        blocksToCheck.add(BlockHelper.getAbsoluteBlock(thisBlock, new RelativeLocation(0,-1,0), null, null));
        boolean valid = false;
        for (Block block : blocksToCheck) {
            int lightLevel = 0;
            if (!block.getBlockData().getMaterial().isSolid()) {
                lightLevel = block.getLightLevel();
            }
            // if light level matches, return true
            if (storage.hasLightLevelMinimum()) {
                if (lightLevel >= storage.getLightLevelMinimum()) {
                    if (!storage.hasLightLevelMaximum()) {
                        return true;
                    }
                    valid = true;
                }
            }
            if (storage.hasLightLevelMaximum()) {
                if (lightLevel > storage.getLightLevelMaximum()) {
                    return false;
                }
                if (!valid && !storage.hasLightLevelMinimum()) {
                    valid = true;
                }
            }
        }
        return valid;
    }

    boolean checkTimePresent(Block thisBlock, RequirementStorage storage) {
        long time = getBlock().getWorld().getTime();
        if (storage.hasTimeMinimum() && storage.hasTimeMaximum()) {
            if (storage.getTimeMinimum() <= storage.getTimeMaximum()) {
                return time >= storage.getTimeMinimum() && time <= storage.getTimeMaximum();
            } else {
                return time >= storage.getTimeMinimum() || time <= storage.getTimeMaximum();
            }
        }
        if (storage.hasTimeMinimum()) {
            return time >= storage.getTimeMinimum();
        }
        if (storage.hasTimeMaximum()) {
            return time <= storage.getTimeMaximum();
        }
        return false;
    }

    boolean checkTemperaturePresent(Block thisBlock, RequirementStorage storage) {
        double temperature = thisBlock.getTemperature();
        if (storage.hasTemperatureMinimum() && temperature < storage.getTemperatureMinimum()) {
            return false;
        }
        return !storage.hasTemperatureMaximum() || !(temperature > storage.getTemperatureMaximum());
    }

    boolean checkWorldPresent(Block thisBlock, RequirementStorage storage) {
        String world = thisBlock.getWorld().getName();
        if (storage.hasWorldWhitelist()) {
            if (!storage.isInWorldWhitelist(world)) {
                return false;
            }
        }
        if (storage.hasWorldBlacklist()) {
            return !storage.isInWorldBlacklist(world);
        }
        return true;
    }

    boolean checkBiomePresent(Block thisBlock, RequirementStorage storage) {
        Biome biome = thisBlock.getBiome();
        if (storage.hasBiomeWhitelist()) {
            if (!storage.isInBiomeWhitelist(biome)) {
                return false;
            }
        }
        if (storage.hasBiomeBlacklist()) {
            return !storage.isInBiomeBlacklist(biome);
        }
        return true;
    }

    boolean checkEnvironmentPresent(Block thisBlock, RequirementStorage storage) {
        World.Environment environment = thisBlock.getWorld().getEnvironment();
        if (storage.hasEnvironmentWhitelist()) {
            if (!storage.isInEnvironmentWhitelist(environment)) {
                return false;
            }
        }
        if (storage.hasEnvironmentBlacklist()) {
            return !storage.isInEnvironmentBlacklist(environment);
        }
        return true;
    }

    //endregion

    public static PlantBlock wrapBlock(Block block) {
        if (block == null) {
            return null;
        }
        return new PlantBlock(new BlockLocation(block), Plant.wrappedPlant, false);
    }

    @Override
    public String toString() {
        return String.format("PlantBlock (%s) @ %s", plant.getId(), location.toString());
    }
}
