package me.kosinkadink.performantplants.scripting.storage.hooks;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.hooks.HookAction;
import me.kosinkadink.performantplants.hooks.PlantHook;
import me.kosinkadink.performantplants.hooks.PlantHookPlayerDead;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScriptHookPlayerDead extends ScriptHookPlayer {

    public ScriptHookPlayerDead(HookAction action) {
        super(action);
    }

    @Override
    public PlantHook createPlantHook(UUID taskId, Player player, PlantBlock plantBlock) {
        return new PlantHookPlayerDead(taskId, action, createOfflinePlayer(player, plantBlock));
    }
}
