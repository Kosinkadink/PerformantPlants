package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.events.PlantBrokenEvent;
import me.kosinkadink.performantplants.events.PlantChunkLoadedEvent;
import me.kosinkadink.performantplants.events.PlantChunkUnloadedEvent;
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

    @EventHandler
    public void onPlantBroken(PlantBrokenEvent event) {
        // check if plant block has a registered hook
        performantPlants.getTaskManager().triggerPlantBrokenHooks(event.getPlantBlock());
    }

    @EventHandler
    public void onPlantChunkLoaded(PlantChunkLoadedEvent event) {
        // check if plant chunk has a registered hook
        performantPlants.getTaskManager().triggerPlantChunkLoadedHooks(event.getChunk());
    }

    @EventHandler
    public void onPlantChunkUnloaded(PlantChunkUnloadedEvent event) {
        // check if plant chunk has a registered hook
        performantPlants.getTaskManager().triggerPlantChunkUnloadedHooks(event.getChunk());
    }

}
