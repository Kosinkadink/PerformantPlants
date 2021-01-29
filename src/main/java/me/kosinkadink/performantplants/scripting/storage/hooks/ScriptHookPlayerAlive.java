package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerAlive;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

import java.util.UUID;

public class ScriptHookPlayerAlive extends ScriptHookPlayer {

    public ScriptHookPlayerAlive(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, ExecutionContext context) {
        return new PlantHookPlayerAlive(taskId, action, hookConfigId, createOfflinePlayer(context));
    }
}
