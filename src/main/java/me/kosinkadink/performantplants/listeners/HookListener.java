package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class HookListener implements Listener {

    private final Main main;

    public HookListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if player has a registered hook
        main.getTaskManager().triggerPlayerOnlineHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // check if player has a registered hook
        main.getTaskManager().triggerPlayerAliveHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // check if player has a registered hook
        main.getTaskManager().triggerPlayerOfflineHooks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // check if player has a registered hook
        main.getTaskManager().triggerPlayerDeadHooks(event.getEntity());
    }

}
