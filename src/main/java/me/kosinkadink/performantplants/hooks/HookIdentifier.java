package me.kosinkadink.performantplants.hooks;

import java.util.Objects;
import java.util.UUID;

public class HookIdentifier {

    private final String taskUUID;
    private final String hookConfigId;

    public HookIdentifier(String taskUUID, String hookConfigId) {
        this.taskUUID = taskUUID;
        this.hookConfigId = hookConfigId;
    }

    public HookIdentifier(UUID taskUUID, String hookConfigId) {
        this.taskUUID = taskUUID.toString();
        this.hookConfigId = hookConfigId;
    }


    public String getTaskUUID() {
        return taskUUID;
    }

    public String getHookConfigId() {
        return hookConfigId;
    }

    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false is object is null or not of same class
        if (o == null || getClass() != o.getClass())
            return false;
        HookIdentifier fromO = (HookIdentifier)o;
        // true if all components match, false otherwise
        return taskUUID.equals(fromO.taskUUID) && hookConfigId.equals(fromO.hookConfigId);
    }

    public int hashCode() {
        return Objects.hash(taskUUID, hookConfigId);
    }

    @Override
    public String toString() {
        return taskUUID + ',' + hookConfigId;
    }

}
