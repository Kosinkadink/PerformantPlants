package me.kosinkadink.performantplants.hooks;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class PlantHookPlayer extends PlantHook {

    protected OfflinePlayer offlinePlayer;

    public PlantHookPlayer(UUID taskId, HookAction action, OfflinePlayer offlinePlayer) {
        super(taskId, action);
        this.offlinePlayer = offlinePlayer;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }
}
