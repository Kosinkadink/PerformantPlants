package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantBreakEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.Drop;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

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
                PlantBlock plantBlock = new PlantBlock(blockLocation, event.getPlant(),
                        event.getPlayer().getUniqueId(), event.getGrows());
                main.getPlantManager().addPlantBlock(plantBlock);
            }
        }
    }

    @EventHandler
    public void onPlantBreak(PlantBreakEvent event) {
        if (!event.isCancelled()) {
            main.getLogger().info("Reviewing PlantBreakEvent for block: " + event.getBlock().getLocation().toString());
            destroyPlantBlock(event.getBlock(), event.getPlantBlock(), true);
        }
    }

    // region Block Creation

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getLocation().getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFertilizeEvent(BlockFertilizeEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    //endregion

    // region Block Destruction

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
                event.setCancelled(true);
                destroyPlantBlock(event.getBlock(), false);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // if block that is to explode is Plant, cancel it (plant shouldn't explode)
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
        // otherwise, check if exploded blocks were Plants, and if so destroy them
        else {
            for (Block block : event.blockList()) {
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    destroyPlantBlock(block, false);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // if block that is to explode is Plant, cancel it (plant shouldn't explode)
        if (MetadataHelper.hasPlantBlockMetadata(event.getLocation().getBlock())) {
            event.setCancelled(true);
        }
        // otherwise, check if exploded blocks were Plants, and if so destroy them
        else {
            for (Block block : event.blockList()) {
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    destroyPlantBlock(block, false);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getToBlock()) &&
                !event.getToBlock().getType().isSolid()) {
            Block block = event.getToBlock();
            destroyPlantBlock(block, true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        // if piston to be extended is Plant, cancel it (plant shouldn't extend)
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
        else if (!event.isCancelled()) {
            // check if any pushed blocks were Plants
            for (Block block : event.getBlocks()) {
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    // destroy them
                    destroyPlantBlock(block, true);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // if piston to be retracted is Plant, cancel it (plant shouldn't retract)
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
        // if the piston is sticky, check if any plants are being pulled
        else if (!event.isCancelled() && event.isSticky()) {
            // check if any pulled blocks were Plants
            for (Block block : event.getBlocks()) {
                // also check if the block was solid; non-solid can't get pulled
                if (MetadataHelper.hasPlantBlockMetadata(block) &&
                        block.getType().isSolid()) {
                    destroyPlantBlock(block, true);
                    event.setCancelled(true);
                }
            }
        }
    }

    //endregion

    void destroyPlantBlock(Block block, PlantBlock plantBlock, boolean drops) {
        block.setType(Material.AIR);
        main.getPlantManager().removePlantBlock(plantBlock);
        // TODO: handle drops
        if (drops) {
            ArrayList<Drop> dropsList = plantBlock.getDrops();
            int dropLimit = plantBlock.getDropLimit();
            boolean limited = dropLimit >= 1;
            int dropCount = 0;
            for (Drop drop : dropsList) {
                // if there is a limit and have reached it, stop dropping
                if (limited && dropCount >= dropLimit) {
                    break;
                }
                ItemStack dropStack = drop.generateDrop();
                if (dropStack.getAmount() != 0) {
                    dropCount++;
                    block.getWorld().dropItemNaturally(block.getLocation(), dropStack);
                }
            }
        }
        // TODO: handle children
        // if block's children should be removed, remove them
        if (plantBlock.getBreakChildren()) {
            ArrayList<BlockLocation> childLocations = new ArrayList<>(plantBlock.getChildLocations());
            for (BlockLocation childLocation : childLocations) {
                destroyPlantBlock(childLocation, drops);
            }
        }
    }

    void destroyPlantBlock(Block block, boolean drops) {
        PlantBlock plantBlock = main.getPlantManager().getPlantBlock(block);
        if (plantBlock != null) {
            destroyPlantBlock(block, plantBlock, drops);
        }
    }

    void destroyPlantBlock(BlockLocation blockLocation, boolean drops) {
        PlantBlock plantBlock = main.getPlantManager().getPlantBlock(blockLocation);
        if (plantBlock != null) {
            destroyPlantBlock(plantBlock.getBlock(), plantBlock, drops);
        }
    }

}
