package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.storage.ScriptTask;
import me.kosinkadink.performantplants.tasks.PlantTask;

public class ScriptOperationScheduleTask extends ScriptOperation {

    private final String plantId;
    private final String taskConfigId;

    public ScriptOperationScheduleTask(String plantId, String taskConfigId, ScriptBlock delay, ScriptBlock currentPlayer,
                                       ScriptBlock currentBlock, ScriptBlock playerId, ScriptBlock autostart) {
        super(delay, currentPlayer, currentBlock, playerId, autostart);
        this.plantId = plantId;
        this.taskConfigId = taskConfigId;
        // TODO: hooks?
    }

    public ScriptBlock getDelay() {
        return inputs[0];
    }

    public ScriptBlock getCurrentPlayer() {
        return inputs[1];
    }

    public ScriptBlock getCurrentBlock() {
        return inputs[2];
    }

    public ScriptBlock getPlayerId() {
        return inputs[3];
    }

    public ScriptBlock getAutostart() {
        return inputs[4];
    }

    @Override
    public ScriptResult perform(ExecutionContext context) throws IllegalArgumentException {
        Plant plant = PerformantPlants.getInstance().getPlantTypeManager().getPlantById(plantId);
        if (plant == null) {
            return ScriptResult.EMPTY;
        }
        ScriptTask scriptTask = plant.getScriptTask(taskConfigId);
        if (scriptTask == null) {
            return ScriptResult.EMPTY;
        }
        // clone task to avoid overriding stored default values
        scriptTask = scriptTask.clone();
        // set values with ones set in this operation
        scriptTask.setDelay(getDelay());
        scriptTask.setCurrentPlayer(getCurrentPlayer());
        scriptTask.setPlayerId(getPlayerId());
        scriptTask.setCurrentBlock(getCurrentBlock());
        scriptTask.setAutostart(getAutostart());
        // create plant task
        PlantTask plantTask = scriptTask.createPlantTask(context);
        if (plantTask == null) {
            return ScriptResult.EMPTY;
        }
        // attempt to schedule task and return result
        if (PerformantPlants.getInstance().getTaskManager().scheduleTask(plantTask)) {
            return new ScriptResult(plantTask.getTaskId().toString());
        }
        return ScriptResult.EMPTY;
    }

    @Override
    protected void setType() {
        type = ScriptType.STRING;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {

    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.ACTION;
    }
}
