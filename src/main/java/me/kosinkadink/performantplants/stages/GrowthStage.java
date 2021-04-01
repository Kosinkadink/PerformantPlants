package me.kosinkadink.performantplants.stages;

import me.kosinkadink.performantplants.blocks.GrowthStageBlock;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.RequirementStorage;

import java.util.HashMap;

public class GrowthStage {

    private final String id;
    private final HashMap<String,GrowthStageBlock> blocks = new HashMap<>();
    private ScriptBlock growthTime = new ScriptResult(-1);
    private boolean growthCheckpoint = false;
    private RequirementStorage requirementStorage = new RequirementStorage();
    private ScriptBlock onExecute;
    private ScriptBlock onFail;

    public GrowthStage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public boolean hasValidGrowthTimeSet(ExecutionContext context) {
        return growthTime.loadValue(context).getLongValue() >= 0;
    }

    public long generateGrowthTime(ExecutionContext context) {
        return growthTime.loadValue(context).getLongValue();
    }

    public boolean isGrowthCheckpoint() {
        return growthCheckpoint;
    }

    public void setGrowthCheckpoint(boolean growthCheckpoint) {
        this.growthCheckpoint = growthCheckpoint;
    }

    public ScriptBlock getOnExecute() {
        return onExecute;
    }

    public void setOnExecute(ScriptBlock onExecute) {
        this.onExecute = onExecute;
    }

    public ScriptBlock getOnFail() {
        return onFail;
    }

    public void setOnFail(ScriptBlock onFail) {
        this.onFail = onFail;
    }
}
