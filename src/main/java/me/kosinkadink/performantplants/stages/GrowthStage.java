package me.kosinkadink.performantplants.stages;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.RequirementStorage;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class GrowthStage {

    private final String id;
    private final HashMap<String,GrowthStageBlock> blocks = new HashMap<>();
    private long minGrowthTime = -1;
    private long maxGrowthTime = -1;
    private boolean growthCheckpoint = false;
    private DropStorage dropStorage = new DropStorage();
    private RequirementStorage requirementStorage = new RequirementStorage();
    private PlantInteract onExecute;
    private PlantInteract onFail;

    public GrowthStage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }

    public RequirementStorage getRequirementStorage() {
        return requirementStorage;
    }

    public void setRequirementStorage(RequirementStorage requirementStorage) {
        this.requirementStorage = requirementStorage;
    }

    public void addGrowthStageBlock(GrowthStageBlock block) {
        blocks.put(block.getId(),block);
    }

    public GrowthStageBlock getGrowthStageBlock(String id) {
        return blocks.get(id);
    }

    public HashMap<String,GrowthStageBlock> getBlocks() {
        return blocks;
    }

    public void setMinGrowthTime(long time) {
        minGrowthTime = time;
    }

    public void setMaxGrowthTime(long time) {
        maxGrowthTime = time;
    }

    public boolean hasValidGrowthTimeSet() {
        return minGrowthTime >= 0 && maxGrowthTime >= 0;
    }

    public long generateGrowthTime() {
        if (minGrowthTime >= 0 && maxGrowthTime >= 0) {
            if (minGrowthTime == maxGrowthTime) {
                return minGrowthTime;
            }
            return ThreadLocalRandom.current().nextLong(minGrowthTime, maxGrowthTime + 1);
        }
        return 0;
    }

    public boolean isGrowthCheckpoint() {
        return growthCheckpoint;
    }

    public void setGrowthCheckpoint(boolean growthCheckpoint) {
        this.growthCheckpoint = growthCheckpoint;
    }

    public PlantInteract getOnExecute() {
        return onExecute;
    }

    public void setOnExecute(PlantInteract onExecute) {
        this.onExecute = onExecute;
    }

    public PlantInteract getOnFail() {
        return onFail;
    }

    public void setOnFail(PlantInteract onFail) {
        this.onFail = onFail;
    }
}
