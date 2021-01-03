package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class HookListener implements Listener {

    private final PerformantPlants performantPlants;

    public HookListener(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if player has a registered hook
        performantPlants.getTaskManager().triggerPlayerOnlineHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // check if player has a registered hook
        performantPlants.getTaskManager().triggerPlayerAliveHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // check if player has a registered hook
        performantPlants.getTaskManager().triggerPlayerOfflineHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // check if player has a registered hook
        performantPlants.getTaskManager().triggerPlayerDeadHooks(event.getEntity());
    }

}
