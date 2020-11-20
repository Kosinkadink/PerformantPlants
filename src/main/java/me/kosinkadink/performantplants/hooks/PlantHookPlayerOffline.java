package me.kosinkadink.performantplants.hooks;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlantHookPlayerOffline extends PlantHookPlayer {

    public PlantHookPlayerOffline(UUID taskId, HookAction action, OfflinePlayer offlinePlayer) {
        super(taskId, action, offlinePlayer);
    }

    @Override
    protected boolean meetsConditions() {
        return !offlinePlayer.isOnline();
    }

}
