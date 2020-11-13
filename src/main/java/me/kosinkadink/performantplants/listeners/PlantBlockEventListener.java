package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.plants.RequiredItem;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.util.*;
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
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PlantBlockEventListener implements Listener {

    private Main main;

    public PlantBlockEventListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlantPlace(PlantPlaceEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission(PermissionHelper.Place)) {
                event.setCancelled(true);
                return;
            }
            if (main.getConfigManager().getConfigSettings().isDebug())
                main.getLogger().info("Reviewing PlantPlaceEvent for block: " + event.getBlock().getLocation().toString());
            Block block = event.getBlock();
            if (block.isEmpty()) {
                // check if empty block has plant metadata
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    // if so, destroy; erroneous existence and continue
                    BlockHelper.destroyPlantBlock(main, block, false);
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
                    if (!plantBlock.checkAllRequirements(main)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // set block orientation (only used if orientable block to be placed)
                plantBlock.setBlockYaw(event.getPlayer().getLocation().getYaw());
                main.getPlantManager().addPlantBlock(plantBlock);
                decrementItemStack(itemStack);
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
            if (main.getConfigManager().getConfigSettings().isDebug())
                main.getLogger().info("Reviewing PlantBreakEvent for block: " + event.getBlock().getLocation().toString());
            // track if block should break and if drops should occur
            boolean actuallyBreakBlock = true;
            boolean giveBlockDrops = true;
            // get item in main hand, used to break the block
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            // get PlantInteract behavior for main hand, if any
            PlantInteract plantInteract = event.getPlantBlock().getOnBreak(itemStack);
            if (plantInteract != null) {
                // see if should do
                boolean shouldDo = plantInteract.generateDoIf(event.getPlayer(), event.getPlantBlock());
                boolean onlyBreakOnDo = plantInteract.isOnlyBreakBlockOnDo(event.getPlayer(), event.getPlantBlock());
                boolean onlyEffectsOnDo = plantInteract.isOnlyEffectsOnDo(event.getPlayer(), event.getPlantBlock());
                boolean onlyConsumableEffectsOnDo = plantInteract.isOnlyConsumableEffectsOnDo(event.getPlayer(), event.getPlantBlock());
                // see if drops should occur
                giveBlockDrops = plantInteract.isGiveBlockDropsNull() || plantInteract.isGiveBlockDrops(event.getPlayer(), event.getPlantBlock());
                // determine if block should be broken
                if (!onlyBreakOnDo || shouldDo) {
                    if (!plantInteract.isBreakBlockNull() && !plantInteract.isBreakBlock(event.getPlayer(), event.getPlantBlock())) {
                        actuallyBreakBlock = false;
                    }
                }
                if (!onlyEffectsOnDo || shouldDo) {
                    plantInteract.getEffectStorage().performEffects(event.getBlock(), event.getPlantBlock());
                }
                if (!onlyConsumableEffectsOnDo || shouldDo) {
                    PlantConsumableStorage consumableStorage = plantInteract.getConsumableStorage();
                    // do consumable actions
                    if (consumableStorage != null) {
                        PlantConsumable consumable = consumableStorage.getConsumable(event.getPlayer(), EquipmentSlot.HAND);
                        if (consumable != null) {
                            consumable.getEffectStorage().performEffects(event.getPlayer(), event.getPlantBlock());
                        }
                    }
                }
                // perform all applicable script blocks
                if (shouldDo) {
                    if (plantInteract.getScriptBlockOnDo() != null) {
                        plantInteract.getScriptBlockOnDo().loadValue(event.getPlantBlock(), event.getPlayer());
                    }
                } else {
                    if (plantInteract.getScriptBlockOnNotDo() != null) {
                        plantInteract.getScriptBlockOnNotDo().loadValue(event.getPlantBlock(), event.getPlayer());
                    }
                }
                if (plantInteract.getScriptBlock() != null) {
                    plantInteract.getScriptBlock().loadValue(event.getPlantBlock(), event.getPlayer());
                }
            }
            if (actuallyBreakBlock) {
                BlockHelper.destroyPlantBlock(main, event.getBlock(), event.getPlantBlock(), giveBlockDrops);
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
            if (main.getConfigManager().getConfigSettings().isDebug())
                main.getLogger().info("Reviewing PlantFarmlandTrampleEvent for block: " + event.getBlock().getLocation().toString());
            // set trampled block to dirt for growth requirement check purposes
            event.getTrampledBlock().setType(Material.DIRT);
            if (!event.getPlantBlock().checkGrowthRequirements()) {
                BlockHelper.destroyPlantBlock(main, event.getBlock(), event.getPlantBlock(), true);
            }
            // set it back to farmland to avoid weird effect on player
            // actual turning into dirt should happen due to PlayerInteractEvent not being cancelled
            event.getTrampledBlock().setType(Material.FARMLAND);
        }
    }

    @EventHandler
    public void onPlantInteract(PlantInteractEvent event) {
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
            if (main.getConfigManager().getConfigSettings().isDebug())
                main.getLogger().info("Reviewing PlantInteractEvent for block: " + event.getBlock().getLocation().toString());
            // get item in main hand
            EquipmentSlot hand = EquipmentSlot.HAND;
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            // get PlantInteract to use
            PlantInteract plantInteract;
            if (!event.isUseOnClick()) {
                // get PlantInteract behavior for main hand, if any
                plantInteract = event.getPlantBlock().getOnInteract(itemStack, event.getBlockFace());
                // if no plant interact behavior, try again for the offhand
                if (plantInteract == null) {
                    hand = EquipmentSlot.OFF_HAND;
                    itemStack = event.getPlayer().getInventory().getItemInOffHand();
                    plantInteract = event.getPlantBlock().getOnInteract(itemStack, event.getBlockFace());
                }
            } else {
                plantInteract = event.getPlantBlock().getOnClick(itemStack, event.getBlockFace());
            }
            // if still no plant interact behavior, cancel event and return
            if (plantInteract == null) {
                event.setCancelled(true);
                return;
            }
            PlantConsumableStorage consumableStorage = plantInteract.getConsumableStorage();
            PlantConsumable consumable = null;
            if (consumableStorage != null) {
                consumable = consumableStorage.getConsumable(event.getPlayer(), event.getHand());
                if (consumable == null) {
                    event.setCancelled(true);
                    return;
                }
            }
            // see if should do
            boolean shouldDo = plantInteract.generateDoIf(event.getPlayer(), event.getPlantBlock());
            boolean onlyEffectsOnDo = plantInteract.isOnlyEffectsOnDo(event.getPlayer(), event.getPlantBlock());
            // try to load onlyConsumableEffectsOnDo if consumable exists
            boolean onlyConsumableEffectsOnDo = consumable != null && plantInteract.isOnlyConsumableEffectsOnDo(event.getPlayer(), event.getPlantBlock());
            boolean onlyTakeItemOnDo = plantInteract.isOnlyTakeItemOnDo(event.getPlayer(), event.getPlantBlock());
            boolean onlyBreakOnDo = plantInteract.isOnlyBreakBlockOnDo(event.getPlayer(), event.getPlantBlock());
            boolean onlyDropOnDo = plantInteract.isOnlyDropOnDo(event.getPlayer(), event.getPlantBlock());
            // break block, if applicable
            if (!onlyBreakOnDo || shouldDo) {
                if (plantInteract.isBreakBlock(event.getPlayer(), event.getPlantBlock())) {
                    BlockHelper.destroyPlantBlock(main, event.getBlock(), event.getPlantBlock(), plantInteract.isGiveBlockDrops(event.getPlayer(), event.getPlantBlock()));
                }
            }
            // drop items, if applicable
            if (!onlyDropOnDo || shouldDo) {
                DropHelper.performDrops(plantInteract.getDropStorage(), event.getBlock(), event.getPlayer(), event.getPlantBlock());
            }
            // take item, if applicable
            if (!onlyTakeItemOnDo || shouldDo) {
                if (plantInteract.isTakeItem(event.getPlayer(), event.getPlantBlock())) {
                    decrementItemStack(itemStack);
                }
            }
            // do break actions for block
            if (!onlyEffectsOnDo || shouldDo) {
                plantInteract.getEffectStorage().performEffects(event.getBlock(), event.getPlantBlock());
            }
            if (!onlyConsumableEffectsOnDo || shouldDo) {
                if (consumable != null) {
                    consumable.getEffectStorage().performEffects(event.getPlayer(), event.getPlantBlock());
                }
            }
            // perform all applicable script blocks
            if (shouldDo) {
                if (plantInteract.getScriptBlockOnDo() != null) {
                    plantInteract.getScriptBlockOnDo().loadValue(event.getPlantBlock(), event.getPlayer());
                }
            } else {
                if (plantInteract.getScriptBlockOnNotDo() != null) {
                    plantInteract.getScriptBlockOnNotDo().loadValue(event.getPlantBlock(), event.getPlayer());
                }
            }
            if (plantInteract.getScriptBlock() != null) {
                plantInteract.getScriptBlock().loadValue(event.getPlantBlock(), event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlantConsume(PlantConsumeEvent event) {
        // check if player has permission
        if (!event.getPlayer().hasPermission(PermissionHelper.Consume)) {
            event.setCancelled(true);
            return;
        }
        if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlantConsumeEvent for item");
        PlantConsumable plantConsumable = event.getConsumable();
        if (plantConsumable == null) {
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Consumable not found for current inventory status");
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
        // do actions stored in item's PlantConsumable
        for (ItemStack itemToGive : plantConsumable.getItemsToGive()) {
            // check that the items could be added
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemToGive);
            for (ItemStack itemToAdd : remaining.values()) {
                DropHelper.emulatePlayerDrop(event.getPlayer(), itemToAdd);
            }
        }
        // decrement item, if set
        if (plantConsumable.isTakeItem()) {
            decrementItemStack(callStack);
        }
        // add damage to item, if set
        if (plantConsumable.getAddDamage() != 0) {
            ItemHelper.updateDamage(callStack, plantConsumable.getAddDamage());
        }
        // decrement required items, if set
        for (RequiredItem requirement : plantConsumable.getRequiredItems()) {
            if (requirement.isTakeItem()) {
                // if should be in hand, decrement other hand's stack
                if (requirement.isInHand()) {
                    decrementItemStack(otherStack);
                }
                // otherwise take required item out of inventory
                else {
                    ItemStack removeStack = requirement.getItemStack().clone();
                    removeStack.setAmount(1);
                    event.getPlayer().getInventory().removeItem(removeStack);
                }
            }
            if (requirement.getAddDamage() != 0) {
                if (requirement.isInHand()) {
                    ItemHelper.updateDamage(otherStack, requirement.getAddDamage());
                }
                else {
                    int slot;
                    if (requirement.getItemStack().getItemMeta() instanceof Damageable) {
                        slot = event.getPlayer().getInventory().first(requirement.getItemStack().getType());
                    } else {
                        slot = event.getPlayer().getInventory().first(requirement.getItemStack());
                    }
                    if (slot >= 0) {
                        ItemStack slotStack = event.getPlayer().getInventory().getItem(slot);
                        if (slotStack != null) {
                            ItemHelper.updateDamage(slotStack, requirement.getAddDamage());
                        }
                    }
                }
            }
        }
        // perform effects
        plantConsumable.getEffectStorage().performEffects(event.getPlayer(), null);
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
                BlockHelper.destroyPlantBlock(main, event.getBlock(), false);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                BlockHelper.destroyPlantBlock(main, block, false);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                BlockHelper.destroyPlantBlock(main, block, false);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getToBlock()) &&
                !event.getToBlock().getType().isSolid()) {
            Block block = event.getToBlock();
            BlockHelper.destroyPlantBlock(main, block, true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (MetadataHelper.hasPlantBlockMetadata(block)) {
            Block source = event.getSourceBlock();
            // only destroy block if source is now air, block is not solid, and source is below block
            if (source.getType().isAir() &&
                    !block.getType().isSolid() &&
                    block.getLocation().getBlockY()-source.getLocation().getBlockY() > 0) {
                BlockHelper.destroyPlantBlock(main, block, true);
                // only destroy source block if it is a plant block
                if (MetadataHelper.hasPlantBlockMetadata(source)) {
                    BlockHelper.destroyPlantBlock(main, source, true);
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
                    BlockHelper.destroyPlantBlock(main, eventBlocks.get(0), true);
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
                        BlockHelper.destroyPlantBlock(main, plantBlock, true);
                    }
                }
            }
        }
    }

    void decrementItemStack(ItemStack itemStack) {
        // if material is air, do nothing
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
    }

}
