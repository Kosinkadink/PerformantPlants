package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantBreakEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.locations.BlockLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlantBlockEventListener implements Listener {

    private Main main;

    public PlantBlockEventListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlantPlace(PlantPlaceEvent event) {
        if (!event.isCancelled()) {
            main.getLogger().info("Reviewing PlantPlaceEvent for block: " + event.getBlock().getLocation().toString());
            Block block = event.getBlock();
            if (block.getType() == Material.AIR) {
                // get item in main hand
                ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                if (itemStack.getType() == Material.AIR) {
                    return;
                }
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
                else {
                    event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
                BlockLocation blockLocation = new BlockLocation(block);
                PlantBlock plantBlock = new PlantBlock(blockLocation, event.getPlant());
                main.getPlantManager().addPlantBlock(plantBlock);
                block.setType(Material.CRACKED_STONE_BRICKS);
            }
        }
    }

    @EventHandler
    public void onPlantBreak(PlantBreakEvent event) {
        if (!event.isCancelled()) {
            main.getLogger().info("Reviewing PlantBreakEvent for block: " + event.getBlock().getLocation().toString());
            // TODO: manage children blocks if applicable
            // set block to air
            event.getBlock().setType(Material.AIR);
            // TODO: create drops
            // remove plantBlock from plantManager
            main.getPlantManager().removePlantBlock(event.getPlantBlock());
        }
    }
}
