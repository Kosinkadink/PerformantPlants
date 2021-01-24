package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlantChunkLoaded;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptHookPlantChunkLoaded extends ScriptHookPlantChunk {

    public ScriptHookPlantChunkLoaded(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock) {
        return new PlantHookPlantChunkLoaded(taskId, action, hookConfigId, createChunkLocation(player, plantBlock));
    }
}
