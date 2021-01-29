package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlantBlockBroken;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

import java.util.UUID;

public class ScriptHookPlantBlockBroken extends ScriptHookPlantBlock {

    public ScriptHookPlantBlockBroken(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, ExecutionContext context) {
        return new PlantHookPlantBlockBroken(taskId, action, hookConfigId, createBlockLocation(context));
    }
}
