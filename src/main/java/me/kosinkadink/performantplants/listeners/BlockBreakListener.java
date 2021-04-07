package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.AnchorBlock;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantBreakEvent;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final PerformantPlants performantPlants;

    public BlockBreakListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // check that event is not cancelled
        if (!event.isCancelled()) {
            // check if has plant block metadata
            if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
                event.setCancelled(true);
                PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(event.getBlock());
                // if plant block is registered, call plant break event
                if (plantBlock != null) {
                    performantPlants.getServer().getPluginManager().callEvent(
                            new PlantBreakEvent(event.getPlayer(), plantBlock, event.getBlock())
                    );
                }
                // otherwise, remove faulty metadata
                else {
                    MetadataHelper.removePlantBlockMetadata(performantPlants, event.getBlock());
                }
            }
            // check if has anchor block metadata
            else if (MetadataHelper.hasAnchorBlockMetadata(event.getBlock())) {
                BlockHelper.destroyAnchoredBlocks(performantPlants, event.getBlock());
            }
        }
    }

}
