package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.Main;

import java.util.Objects;
import java.util.UUID;

public abstract class PlantHook {

    protected final UUID hookId;
    protected final UUID taskId;
    protected final HookAction action;

    public PlantHook(UUID hookId, UUID taskId, HookAction action) {
        this.hookId = hookId;
        this.taskId = taskId;
        this.action = action;
    }

    public PlantHook(UUID taskId, HookAction action) {
        hookId = UUID.randomUUID();
        this.taskId = taskId;
        this.action = action;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public HookAction getAction() {
        return action;
    }

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
                Main.getInstance().getTaskManager().resumeTask(taskId.toString());
                return true;
            case PAUSE:
                Main.getInstance().getTaskManager().pauseTask(taskId.toString());
                return true;
            case CANCEL:
                Main.getInstance().getTaskManager().cancelTask(taskId.toString());
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
        return hookId == fromO.hookId && taskId == fromO.taskId;
    }

    public int hashCode() {
        return Objects.hash(hookId, taskId);
    }

}
