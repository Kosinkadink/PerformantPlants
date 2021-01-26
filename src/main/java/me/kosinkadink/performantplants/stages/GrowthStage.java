package me.kosinkadink.performantplants.stages;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.RequirementStorage;

import java.util.HashMap;

public class GrowthStage {

    private final String id;
    private final HashMap<String,GrowthStageBlock> blocks = new HashMap<>();
    private ScriptBlock growthTime = new ScriptResult(-1);
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

    public void setGrowthTime(ScriptBlock growthTime) {
        this.growthTime = growthTime;
    }

    public boolean hasValidGrowthTimeSet(PlantBlock plantBlock) {
        return growthTime.loadValue(plantBlock).getLongValue() >= 0;
    }

    public long generateGrowthTime(PlantBlock plantBlock) {
        return growthTime.loadValue(plantBlock).getLongValue();
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
