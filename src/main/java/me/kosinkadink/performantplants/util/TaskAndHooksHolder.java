package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.hooks.HookIdentifier;

import java.util.ArrayList;
import java.util.List;

public class TaskAndHooksHolder {

    private final String taskUUID;
    private List<HookIdentifier> hookIdentifiers = new ArrayList<>();

    public TaskAndHooksHolder(String taskUUID, List<HookIdentifier> hookIdentifier) {
        this.taskUUID = taskUUID;
        this.hookIdentifiers = hookIdentifier;
    }

    public TaskAndHooksHolder(String taskUUID) {
        this.taskUUID = taskUUID;
    }

    public TaskAndHooksHolder(List<HookIdentifier> hookIdentifiers) {
        this.taskUUID = null;
        this.hookIdentifiers = hookIdentifiers;
    }

    public String getTaskUUID() {
        return taskUUID;
    }

    public List<HookIdentifier> getHookIdentifiers() {
        return hookIdentifiers;
    }

}
