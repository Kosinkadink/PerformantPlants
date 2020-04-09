package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantBreakEvent;
import me.kosinkadink.performantplants.util.MetadataHelper;
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
        // check that event is not cancelled and block has right metadata
        if (!event.isCancelled() &&
                MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
            PlantBlock plantBlock = main.getPlantManager().getPlantBlock(event.getBlock());
            // if plant block is registered, call plant break event
            if (plantBlock != null) {
                main.getServer().getPluginManager().callEvent(
                        new PlantBreakEvent(event.getPlayer(), plantBlock, event.getBlock())
                );
            }
            // otherwise, remove faulty metadata
            else {
                MetadataHelper.removePlantBlockMetadata(main, event.getBlock());
            }
        }
    }

}
