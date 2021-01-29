package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerDead;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

import java.util.UUID;

public class ScriptHookPlayerDead extends ScriptHookPlayer {

    public ScriptHookPlayerDead(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, ExecutionContext context) {
        return new PlantHookPlayerDead(taskId, action, hookConfigId, createOfflinePlayer(context));
    }
}
