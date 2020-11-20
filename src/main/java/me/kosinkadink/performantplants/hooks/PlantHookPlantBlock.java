package me.kosinkadink.performantplants.hooks;

import java.util.UUID;

public abstract class PlantHookPlantBlock extends PlantHook {
    public PlantHookPlantBlock(UUID taskId, HookAction action) {
        super(taskId, action);
    }
}
