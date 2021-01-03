package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlantHookPlayerDead extends PlantHookPlayer {

    public PlantHookPlayerDead(UUID taskId, HookAction action, String hookConfigId, OfflinePlayer offlinePlayer) {
        super(taskId, action, hookConfigId, offlinePlayer);
    }

    public PlantHookPlayerDead(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId, jsonString);
    }

    @Override
    protected boolean meetsConditions() {
        if (offlinePlayer.isOnline()) {
            Player player = (Player)offlinePlayer;
            return !PlayerHelper.isAlive(player);
        }
        return false;
    }

}
