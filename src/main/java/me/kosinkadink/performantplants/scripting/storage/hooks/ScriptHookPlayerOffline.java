package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerOffline;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

import java.util.UUID;

public class ScriptHookPlayerOffline extends ScriptHookPlayer {

    public ScriptHookPlayerOffline(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, ExecutionContext context) {
        return new PlantHookPlayerOffline(taskId, action, hookConfigId, createOfflinePlayer(context));
    }
}
