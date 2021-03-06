package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.PermissionHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlantBlockEventListener implements Listener {

    private final PerformantPlants performantPlants;

    public PlantBlockEventListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onPlantPlace(PlantPlaceEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission(PermissionHelper.Place)) {
                event.setCancelled(true);
                return;
            }
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlantPlaceEvent for block: " + event.getBlock().getLocation().toString());
            Block block = event.getBlock();
            if (block.isEmpty()) {
                // check if empty block has plant metadata
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    // if so, destroy; erroneous existence and continue
                    BlockHelper.destroyPlantBlock(performantPlants, block, false);
                }
                // get item in appropriate hand hand
                ItemStack itemStack;
                if (event.getHand() == EquipmentSlot.OFF_HAND) {
                    itemStack = event.getPlayer().getInventory().getItemInOffHand();
                } else {
                    itemStack = event.getPlayer().getInventory().getItemInMainHand();
                }
                // do nothing if slot is empty
                if (itemStack.getType() == Material.AIR) {
                    event.setCancelled(true);
                    return;
                }
                BlockLocation blockLocation = new BlockLocation(block);
                PlantBlock plantBlock = new PlantBlock(blockLocation, event.getPlant(),
                        event.getPlayer().getUniqueId(), event.getGrows());
                if (plantBlock.isGrows()) {
                    // set newly placed; will check plant requirements instead of growth requirements, if present
                    plantBlock.setNewlyPlaced(true);
                    if (!plantBlock.checkAllRequirements(performantPlants)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // set block orientation (only used if orientable block to be placed)
                plantBlock.setBlockYaw(event.getPlayer().getLocation().getYaw());
                performantPlants.getPlantManager().addPlantBlock(plantBlock);
                ItemHelper.decrementItemStack(itemStack);
            }
        }
    }

    @EventHandler
    public void onPlantBreak(PlantBreakEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission(PermissionHelper.Break)) {
                event.setCancelled(true);
                return;
            }
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlantBreakEvent for block: " + event.getBlock().getLocation().toString());
            // get item in main hand, used to break the block
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            // get PlantInteract behavior for main hand, if any
            ExecutionContext context = new ExecutionContext()
                    .set(event.getPlayer())
                    .set(event.getPlantBlock())
                    .set(itemStack);
            ScriptBlock plantInteract = event.getPlantBlock().getOnBreak();
            if (plantInteract != null) {
                boolean performed = plantInteract.loadValue(context).getBooleanValue();
                if (!performed) {
                    event.setCancelled(true);
                }
            }
            else {
                BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), event.getPlantBlock(), true);
                event.setBlockBroken(true);
            }
        }
    }

    @EventHandler
    public void onPlantFarmlandTrample(PlantFarmlandTrampleEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission(PermissionHelper.Interact)) {
                event.setCancelled(true);
                return;
            }
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlantFarmlandTrampleEvent for block: " + event.getBlock().getLocation().toString());
            // set trampled block to dirt for growth requirement check purposes
            event.getTrampledBlock().setType(Material.DIRT);
            if (!event.getPlantBlock().checkGrowthRequirements()) {
                BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), event.getPlantBlock(), true);
            }
            // set it back to farmland to avoid weird effect on player
            // actual turning into dirt should happen due to PlayerInteractEvent not being cancelled
            event.getTrampledBlock().setType(Material.FARMLAND);
        }
    }

    @EventHandler
    public void onPlantBlockInteract(PlantBlockInteractEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission(PermissionHelper.Interact)) {
                event.setCancelled(true);
                return;
            }
            // don't do anything if offhand triggered event; this listener processes both hands
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                event.setCancelled(true);
                return;
            }
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlantInteractEvent for block: " + event.getBlock().getLocation().toString());
            // create context
            ExecutionContext context = new ExecutionContext()
                    .set(event.getPlayer())
                    .set(event.getPlantBlock())
                    .set(event.getBlockFace());
            // get item in main hand
            EquipmentSlot hand = EquipmentSlot.HAND;
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            context.set(itemStack).set(hand);
            // get PlantInteract to use
            ScriptBlock plantInteract;
            if (!event.isUseOnClick()) {
                // get PlantInteract behavior for main hand, if any
                plantInteract = event.getPlantBlock().getOnInteract();
                // if no plant interact behavior, try again for the offhand
                if (plantInteract == null) {
                    hand = EquipmentSlot.OFF_HAND;
                    itemStack = event.getPlayer().getInventory().getItemInOffHand();
                    context.set(itemStack).set(hand);
                    plantInteract = event.getPlantBlock().getOnInteract();
                }
            } else {
                plantInteract = event.getPlantBlock().getOnClick();
            }
            // if still no plant interact behavior, cancel event and return
            if (plantInteract == null) {
                event.setCancelled(true);
                return;
            }
            // try to perform actions
            boolean performed = plantInteract.loadValue(context).getBooleanValue();
            if (!performed) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlantItemInteract(PlantItemInteractEvent event) {
        // check if player has permission
        if (!event.getPlayer().hasPermission(PermissionHelper.Consume)) {
            event.setCancelled(true);
            return;
        }
        if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Reviewing PlantConsumeEvent for item");
        ScriptBlock consumable = event.getConsumable();
        if (consumable == null) {
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Consumable is null");
            event.setCancelled(true);
            return;
        }
        ItemStack callStack;
        ItemStack otherStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            callStack = event.getPlayer().getInventory().getItemInOffHand();
            otherStack = event.getPlayer().getInventory().getItemInMainHand();
        } else {
            callStack = event.getPlayer().getInventory().getItemInMainHand();
            otherStack = event.getPlayer().getInventory().getItemInOffHand();
        }

        // see if should do
        ExecutionContext context = new ExecutionContext()
                .set(event.getPlayer())
                .set(callStack)
                .set(event.getHand());

        // try to perform script
        boolean performed = consumable.loadValue(context).getBooleanValue();
        if (!performed) {
            event.setCancelled(true);
        }
    }

    // region Block Creation

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        // check what new state of block is supposed to be
        Material blockMaterial = event.getNewState().getType();
        if (blockMaterial == Material.CACTUS || blockMaterial == Material.SUGAR_CANE) {
            // if sugarcane or cactus, check if block underneath is a plant
            if (MetadataHelper.hasPlantBlockMetadata(event.getBlock().getRelative(BlockFace.DOWN))) {
                event.setCancelled(true);
                return;
            }
        }
        if (blockMaterial == Material.PUMPKIN || blockMaterial == Material.MELON) {
            // if pumpkin or melon, check if any stem blocks around are plants
            ArrayList<Block> blocksToCheck = new ArrayList<>();
            blocksToCheck.add(event.getBlock().getRelative(BlockFace.NORTH));
            blocksToCheck.add(event.getBlock().getRelative(BlockFace.EAST));
            blocksToCheck.add(event.getBlock().getRelative(BlockFace.SOUTH));
            blocksToCheck.add(event.getBlock().getRelative(BlockFace.WEST));
            for (Block block : blocksToCheck) {
                if (block.getType() == Material.PUMPKIN_STEM || block.getType() == Material.MELON_STEM) {
                    if (MetadataHelper.hasPlantBlockMetadata(block)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
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
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock()) || MetadataHelper.hasPlantBlockMetadata(event.getSource())) {
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
                BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), false);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                BlockHelper.destroyPlantBlock(performantPlants, block, false);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                BlockHelper.destroyPlantBlock(performantPlants, block, false);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getToBlock()) &&
                !event.getToBlock().getType().isSolid()) {
            Block block = event.getToBlock();
            BlockHelper.destroyPlantBlock(performantPlants, block, true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (MetadataHelper.hasPlantBlockMetadata(block)) {
            Block source = event.getSourceBlock();
            // only destroy block if source is now air/moving piston, block is not solid, and source is below block
            if ((source.getType().isAir() || source.getType() == Material.MOVING_PISTON) &&
                    !block.getType().isSolid() &&
                    block.getLocation().getBlockY()-source.getLocation().getBlockY() > 0) {
                BlockHelper.destroyPlantBlock(performantPlants, block, true);
                // only destroy source block if it is a plant block
                if (MetadataHelper.hasPlantBlockMetadata(source)) {
                    BlockHelper.destroyPlantBlock(performantPlants, source, true);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    //endregion

    private void handlePistonEvent(BlockPistonEvent event, List<Block> eventBlocks) {
        // if piston to be extended is Plant, cancel it (plant shouldn't extend)
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
        }
        else if (!event.isCancelled()) {
            // check if any pushed blocks were Plants
            boolean anyPlantBlocks = false;
            for (Block block : eventBlocks) {
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    anyPlantBlocks = true;
                    break;
                }
            }
            if (anyPlantBlocks) {
                // destroy any plant blocks immediately being moved by piston
                if (MetadataHelper.hasPlantBlockMetadata(eventBlocks.get(0))) {
                    BlockHelper.destroyPlantBlock(performantPlants, eventBlocks.get(0), true);
                    if (eventBlocks.get(0).getType().isSolid()) {
                        event.setCancelled(true);
                    }
                } else {
                    // otherwise, need to see if solid non-plant blocks are moving any plant blocks
                    HashSet<Block> stickyBlocks = new HashSet<>();
                    HashSet<Block> solidBlocks = new HashSet<>();
                    HashSet<Block> solidPlantBlocks = new HashSet<>();
                    ArrayList<Block> plantBlocks = new ArrayList<>();
                    ArrayList<Block> plantBlocksToDestroy = new ArrayList<>();
                    for (Block block : eventBlocks) {
                        if (MetadataHelper.hasPlantBlockMetadata(block)) {
                            if (block.getType().isSolid()) {
                                solidPlantBlocks.add(block);
                            }
                            plantBlocks.add(block);
                        }
                        // check if sticky block and/or solid
                        else {
                            if (block.getType() == Material.SLIME_BLOCK || block.getType() == Material.HONEY_BLOCK) {
                                stickyBlocks.add(block);
                            }
                            if (block.getType().isSolid()) {
                                solidBlocks.add(block);
                            }
                        }
                    }
                    // destroy any plant blocks moved by a block
                    for (Block plantBlock : plantBlocks) {
                        Block interactingBlock = plantBlock.getRelative(event.getDirection().getOppositeFace());
                        if (solidBlocks.contains(interactingBlock)) {
                            plantBlocksToDestroy.add(plantBlock);
                        } else if (solidPlantBlocks.contains(interactingBlock)) {
                            event.setCancelled(true);
                            return;
                        } else if (plantBlock.getType().isSolid() && !stickyBlocks.isEmpty()) {
                            // if block is moved by a sticky block, destroy it
                            if (stickyBlocks.contains(plantBlock.getRelative(BlockFace.DOWN)) ||
                                    stickyBlocks.contains(plantBlock.getRelative(BlockFace.UP)) ||
                                    stickyBlocks.contains(plantBlock.getRelative(BlockFace.NORTH)) ||
                                    stickyBlocks.contains(plantBlock.getRelative(BlockFace.EAST)) ||
                                    stickyBlocks.contains(plantBlock.getRelative(BlockFace.SOUTH)) ||
                                    stickyBlocks.contains(plantBlock.getRelative(BlockFace.WEST))) {
                                plantBlocksToDestroy.add(plantBlock);
                            }
                        }
                    }
                    // destroy any plant blocks marked for destruction
                    for (Block plantBlock : plantBlocksToDestroy) {
                        BlockHelper.destroyPlantBlock(performantPlants, plantBlock, true);
                    }
                }
            }
        }
    }

}
