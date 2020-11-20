package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerAlive;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptHookPlayerAlive extends ScriptHookPlayer {

    public ScriptHookPlayerAlive(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock) {
        return new PlantHookPlayerAlive(taskId, action, createOfflinePlayer(player, plantBlock));
    }
}
