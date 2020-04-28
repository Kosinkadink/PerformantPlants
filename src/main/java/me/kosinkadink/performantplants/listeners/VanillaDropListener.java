package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class VanillaDropListener implements Listener {

    private Main main;

    public VanillaDropListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        DropStorage storage = main.getVanillaDropManager().getDropStorage(event.getEntityType());
        if (storage != null) {
            DropHelper.performDrops(storage, event.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // do nothing if in creative
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        // do nothing if is a plant block
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            return;
        }
        Block block = event.getBlock();
        // otherwise, check if have custom drops
        DropStorage storage = main.getVanillaDropManager().getDropStorage(block);
        if (storage != null) {
            // drop them if they were set to be dropped
            if (!event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand()).isEmpty()) {
                DropHelper.performDrops(storage, block);
            }
        }
    }

}
