package me.kosinkadink.performantplants.hooks;

import me.kosinkadink.performantplants.exceptions.PlantHookJsonParseException;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlantHookPlayerOnline extends PlantHookPlayer {

    public PlantHookPlayerOnline(UUID taskId, HookAction action, String hookConfigId, OfflinePlayer offlinePlayer) {
        super(taskId, action, hookConfigId, offlinePlayer);
    }

    public PlantHookPlayerOnline(UUID taskId, HookAction action, String hookConfigId, String jsonString) throws PlantHookJsonParseException {
        super(taskId, action, hookConfigId, jsonString);
    }

    @Override
    protected boolean meetsConditions() {
        return offlinePlayer.isOnline();
    }

}
