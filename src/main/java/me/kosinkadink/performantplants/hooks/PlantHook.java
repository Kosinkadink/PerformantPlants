package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.tasks.PlantTask;

import java.util.Objects;
import java.util.UUID;

public abstract class PlantHook {

    protected final UUID taskId;
    protected final HookAction action;

    protected String hookConfigId = "";

    public PlantHook(UUID taskId, HookAction action, String hookConfigId) {
        this.taskId = taskId;
        this.action = action;
        this.hookConfigId = hookConfigId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public HookAction getAction() {
        return action;
    }

    public String getHookConfigId() {
        return hookConfigId;
    }

    public void setHookConfigId(String hookConfigId) {
        if (hookConfigId == null) {
            hookConfigId = "";
        }
        this.hookConfigId = hookConfigId;
    }

    public abstract String createJsonString();

    public abstract void loadValuesFromJsonString(String jsonString) throws PlantHookJsonParseException;

    public abstract boolean performScriptBlock(PlantTask task);

    /**
     * @return false if task got cancelled, true otherwise
     */
    public boolean performStartup() {
        if (meetsConditions()) {
            return doActionOnTask();
        }
        return true;
    }

    protected boolean meetsConditions() {
        return false;
    }

    private boolean doActionOnTask() {
        switch(action) {
            case START:
                PerformantPlants.getInstance().getTaskManager().resumeTask(taskId.toString(), this);
                return true;
            case PAUSE:
                PerformantPlants.getInstance().getTaskManager().pauseTask(taskId.toString(), this);
                return true;
            case CANCEL:
                PerformantPlants.getInstance().getTaskManager().cancelTask(taskId.toString(), this);
                return false;
            default:
                return false;
        }
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        PlantHook fromO = (PlantHook)o;
        // true if hookId and taskId match
        return taskId == fromO.taskId && hookConfigId.equals(fromO.hookConfigId);
    }

    public int hashCode() {
        return Objects.hash(taskId, hookConfigId);
    }

}
