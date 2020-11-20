package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerAlive;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerOnline;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptHookPlayerOnline extends ScriptHookPlayer {

    public ScriptHookPlayerOnline(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock) {
        return new PlantHookPlayerOnline(taskId, action, createOfflinePlayer(player, plantBlock));
    }
}
