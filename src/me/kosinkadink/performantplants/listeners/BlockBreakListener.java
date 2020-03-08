package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private Main main;

    public BlockBreakListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // do stuff here
    }

}
