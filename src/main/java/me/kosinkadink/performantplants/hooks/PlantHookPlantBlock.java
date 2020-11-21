package me.kosinkadink.performantplants.hooks;

import java.util.UUID;

public abstract class PlantHookPlantBlock extends PlantHook {
    public PlantHookPlantBlock(UUID taskId, HookAction action, String hookConfigId) {
        super(taskId, action, hookConfigId);
    }
}
