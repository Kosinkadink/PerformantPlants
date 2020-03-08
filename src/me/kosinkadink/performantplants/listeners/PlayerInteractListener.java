package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private Main main;

    public PlayerInteractListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // do stuff here
    }
}
