package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationCancelTask extends ScriptOperationAction {

    public ScriptOperationCancelTask(ScriptBlock taskId) {
        super(taskId);
    }

    public ScriptBlock getTaskId() {
        return inputs[0];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        String taskId = getTaskId().loadValue(context).getStringValue();
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
}
