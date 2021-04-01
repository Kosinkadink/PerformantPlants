package me.kosinkadink.performantplants.listeners;

import com.google.common.collect.Lists;
import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.DestroyReason;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.util.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
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
import java.util.ListIterator;

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
            if (block.isEmpty() || (event.getReplacedState() != null && event.getReplacedState().getType().isAir())) {
                // check if empty block has plant metadata
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    // if so, destroy; erroneous existence and continue
                    BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.REPLACE, null);
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
                    // replace block with air to properly check space requirements, if replaced state is set
                    if (event.getReplacedState() != null) {
                        block.setType(Material.AIR);
                    }
                    // if random rotate, try possible orientations until success or exhaustion
                    if (plantBlock.getPlant().isRandomRotate()) {
                        List<BlockFace> blockFaces = Lists.newArrayList(BlockHelper.getDirectionalBlockFaces());
                        int fullSize = blockFaces.size();
                        for (int i = 0; i < fullSize; i++) {
                            int randomIndex = RandomHelper.generateRandomIntInRange(0, blockFaces.size()-1);
                            plantBlock.setBlockYaw(BlockHelper.getYawFromRotation(blockFaces.get(randomIndex)));
                            // set newly placed; will check plant requirements instead of growth requirements, if present
                            plantBlock.setNewlyPlaced(true);
                            if (plantBlock.checkAllRequirements(performantPlants)) {
                                break;
                            }
                            blockFaces.remove(randomIndex);
                        }
                        // if list is empty, then no success
                        if (blockFaces.isEmpty()) {
                            event.setCancelled(true);
                            if (event.getReplacedState() != null) {
                                block.setBlockData(event.getReplacedState().getBlockData());
                            }
                            return;
                        }
                    } else {
                        // set block orientation
                        plantBlock.setBlockYaw(event.getPlayer().getLocation().getYaw());
                        // set newly placed; will check plant requirements instead of growth requirements, if present
                        plantBlock.setNewlyPlaced(true);
                        if (!plantBlock.checkAllRequirements(performantPlants)) {
                            event.setCancelled(true);
                            if (event.getReplacedState() != null) {
                                block.setBlockData(event.getReplacedState().getBlockData());
                            }
                            return;
                        }
                    }
                }
                performantPlants.getPlantManager().addPlantBlock(plantBlock);
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    ItemHelper.decrementItemStack(itemStack);
                }
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

            boolean destroyed = BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), event.getPlantBlock(), DestroyReason.BREAK, context);
            if (destroyed) {
                event.setBlockBroken(true);
            } else {
                event.setCancelled(true);
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
                ExecutionContext context = new ExecutionContext()
                        .set(event.getPlayer())
                        .set(event.getPlantBlock());
                BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), event.getPlantBlock(), DestroyReason.RELATIVE_BREAK, context);
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
            // get PlantInteract to use
            ScriptBlock plantInteract;
            if (event.isUseOnClick()) {
                // get click behavior (left click)
                plantInteract = event.getPlantBlock().getOnClick();
                // if no behavior, cancel event and return
                if (plantInteract == null) {
                    event.setCancelled(true);
                    return;
                }
                // use item in main hand
                ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                context.set(itemStack).set(EquipmentSlot.HAND);
                // try to perform actions for main hand
                boolean performed = plantInteract.loadValue(context).getBooleanValue();
                if (!performed) {
                    event.setCancelled(true);
                }
            } else {
                // get interact behavior (right click)
                plantInteract = event.getPlantBlock().getOnInteract();
                // if no behavior, cancel event and return
                if (plantInteract == null) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                ItemStack otherStack = event.getPlayer().getInventory().getItemInOffHand();
                if (!itemStack.getType().isAir() || otherStack.getType().isAir()) {
                    // use item in main hand
                    context.set(itemStack).set(EquipmentSlot.HAND);
                } else {
                    // use item in offhand
                    context.set(otherStack).set(EquipmentSlot.OFF_HAND);
                }
                // try to perform actions
                boolean performed = plantInteract.loadValue(context).getBooleanValue();
                if (!performed) {
                    event.setCancelled(true);
                }
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
        if (!event.isCancelled() && MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
            BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), DestroyReason.FADE, null);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!event.isCancelled() && MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
            BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), DestroyReason.DECAY, null);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled() && MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            event.setCancelled(true);
            BlockHelper.destroyPlantBlock(performantPlants, event.getBlock(), DestroyReason.BURN, null);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!event.isCancelled()) {
            // check if exploded blocks were Plants, and if so destroy them
            ListIterator<Block> iterator = event.blockList().listIterator();
            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    iterator.remove();
                    BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.EXPLODE, null);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!event.isCancelled()) {
            // check if exploded blocks were Plants, and if so destroy them
            ListIterator<Block> iterator = event.blockList().listIterator();
            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    iterator.remove();
                    BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.EXPLODE, null);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!event.isCancelled() && MetadataHelper.hasPlantBlockMetadata(event.getToBlock()) &&
                !event.getToBlock().getType().isSolid()) {
            Block block = event.getToBlock();
            BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.RELATIVE_BREAK, null);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!event.isCancelled() && MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            Block block = event.getBlock();
            Block source = event.getSourceBlock();
            if (block.getBlockData() instanceof FaceAttachable && source.getType().isAir() &&
                    MetadataHelper.hasPlantBlockMetadata(source) && MetadataHelper.haveMatchingPlantMetadata(block, source)) {
                BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.RELATIVE_BREAK, null);
            }
            // only destroy block if source is now air/moving piston, block is not solid, and source is below block
            else if ((source.getType().isAir() || source.getType() == Material.MOVING_PISTON) &&
                    !block.getType().isSolid() &&
                    block.getLocation().getBlockY()-source.getLocation().getBlockY() > 0) {
                // remove if block not FaceAttachable or if it's attached to floor of destroyed block
                if (!(block.getBlockData() instanceof FaceAttachable) ||
                        ((FaceAttachable)block.getBlockData()).getAttachedFace() == FaceAttachable.AttachedFace.FLOOR) {
                    BlockHelper.destroyPlantBlock(performantPlants, block, DestroyReason.RELATIVE_BREAK, null);
                    // only destroy source block if it is a plant block
                    if (MetadataHelper.hasPlantBlockMetadata(source)) {
                        BlockHelper.destroyPlantBlock(performantPlants, source, DestroyReason.RELATIVE_BREAK, null);
                    }
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
        if (!event.isCancelled()) {
            if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
                event.setCancelled(true);
                return;
            }
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
                    BlockHelper.destroyPlantBlock(performantPlants, eventBlocks.get(0), DestroyReason.PISTON, null);
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
                        boolean destroyed = BlockHelper.destroyPlantBlock(performantPlants, plantBlock, DestroyReason.PISTON, null);
                        // if block couldn't be destroyed for whatever reason, cancel event and don't destroy any other blocks
                        if (!destroyed) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

}
