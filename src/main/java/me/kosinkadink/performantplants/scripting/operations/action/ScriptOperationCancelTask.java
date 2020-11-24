package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.entity.Player;

public class ScriptOperationCancelTask extends ScriptOperation {

    public ScriptOperationCancelTask(ScriptBlock taskId) {
        super(taskId);
    }

    public ScriptBlock getTaskId() {
        return inputs[0];
    }

    @Override
    public ScriptResult perform(PlantBlock plantBlock, Player player) throws IllegalArgumentException {
        String taskId = getTaskId().loadValue(plantBlock, player).getStringValue();
        if (taskId.isEmpty()) {
            return ScriptResult.FALSE;
        }
        // return whether cancellation was successful
        return new ScriptResult(PerformantPlants.getInstance().getTaskManager().cancelTask(taskId, null));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
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
