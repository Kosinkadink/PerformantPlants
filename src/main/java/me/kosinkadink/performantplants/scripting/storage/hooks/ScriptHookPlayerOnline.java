package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerOnline;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

import java.util.UUID;

public class ScriptHookPlayerOnline extends ScriptHookPlayer {

    public ScriptHookPlayerOnline(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, ExecutionContext context) {
        return new PlantHookPlayerOnline(taskId, action, hookConfigId, createOfflinePlayer(context));
    }
}
