package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.*;
import me.kosinkadink.performantplants.locations.BlockLocation;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.plants.RequiredItem;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.StageStorage;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
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

public class PlantBlockEventListener implements Listener {

    private Main main;

    public PlantBlockEventListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlantPlace(PlantPlaceEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission("performantplants.place")) {
                event.setCancelled(true);
                return;
            }
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlantPlaceEvent for block: " + event.getBlock().getLocation().toString());
            Block block = event.getBlock();
            if (block.isEmpty()) {
                // check if empty block has plant metadata
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    // if so, destroy; erroneous existence and continue
                    destroyPlantBlock(block, false);
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
                if (plantBlock.getGrows()) {
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
            if (!event.getPlayer().hasPermission("performantplants.break")) {
                event.setCancelled(true);
                return;
            }
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlantBreakEvent for block: " + event.getBlock().getLocation().toString());
            destroyPlantBlock(event.getBlock(), event.getPlantBlock(), true);
            // get item in main hand, used to break the block
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            // get PlantInteract behavior for main hand, if any
            PlantInteract plantInteract = event.getPlantBlock().getOnBreak(itemStack);
            if (plantInteract != null) {
                // see if randomly generated chance is okay
                boolean chanceSuccess = plantInteract.generateChance();
                // do break actions for block
                if (!plantInteract.isOnlyEffectsOnChance() || chanceSuccess) {
                    plantInteract.getEffectStorage().performEffects(event.getBlock(), event.getPlantBlock());
                }
                PlantConsumableStorage consumableStorage = plantInteract.getConsumableStorage();
                // do consumable actions
                if (consumableStorage != null) {
                    PlantConsumable consumable = consumableStorage.getConsumable(event.getPlayer(), EquipmentSlot.HAND);
                    if (consumable != null) {
                        consumable.getEffectStorage().performEffects(event.getPlayer(), event.getPlantBlock());
                    }
                }
                // perform script block, if present
                if (plantInteract.getScriptBlock() != null) {
                    plantInteract.getScriptBlock().loadValue(event.getPlantBlock(), event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlantFarmlandTrample(PlantFarmlandTrampleEvent event) {
        if (!event.isCancelled()) {
            // check if player has permission
            if (!event.getPlayer().hasPermission("performantplants.interact")) {
                event.setCancelled(true);
                return;
            }
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlantFarmlandTrampleEvent for block: " + event.getBlock().getLocation().toString());
            // set trampled block to dirt for growth requirement check purposes
            event.getTrampledBlock().setType(Material.DIRT);
            if (!event.getPlantBlock().checkGrowthRequirements(main)) {
                destroyPlantBlock(event.getBlock(), event.getPlantBlock(), true);
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
            if (!event.getPlayer().hasPermission("performantplants.interact")) {
                event.setCancelled(true);
                return;
            }
            // don't do anything if offhand triggered event; this listener processes both hands
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                event.setCancelled(true);
                return;
            }
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlantInteractEvent for block: " + event.getBlock().getLocation().toString());
            // get item in main hand
            EquipmentSlot hand = EquipmentSlot.HAND;
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            // get PlantInteract behavior for main hand, if any
            PlantInteract plantInteract = event.getPlantBlock().getOnInteract(itemStack);
            // if no plant interact behavior, try again for the offhand
            if (plantInteract == null) {
                hand = EquipmentSlot.OFF_HAND;
                itemStack = event.getPlayer().getInventory().getItemInOffHand();
                plantInteract = event.getPlantBlock().getOnInteract(itemStack);
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
            // see if randomly generated chance is okay
            boolean chanceSuccess = plantInteract.generateChance();
            if (chanceSuccess) {
                // if specific growth stage is given to advance to, change growth stage
                if (plantInteract.isChangeStage()) {
                    boolean success = false;
                    if (plantInteract.getGoToStage() != null) {
                        StageStorage stageStorage = event.getPlantBlock().getPlant().getStageStorage();
                        if (stageStorage.isValidStage(plantInteract.getGoToStage())) {
                            int stageIndex = stageStorage.getGrowthStageIndex(plantInteract.getGoToStage());
                            success = event.getPlantBlock().goToStageForcefully(main, stageIndex);
                        }
                    }
                    // if goToNext is set to true, then advance to next growth stage as if plant grew
                    else if (plantInteract.isGoToNext()) {
                        success = event.getPlantBlock().goToNextStage(main);
                    }
                    // if not successfully changed stage, cancel event and do nothing
                    if (!success) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // break block, if applicable
                if (plantInteract.isBreakBlock()) {
                    // drop block items, if applicable
                    destroyPlantBlock(event.getBlock(), event.getPlantBlock(), plantInteract.isGiveBlockDrops());
                }
                // drop interact items, if any
                DropHelper.performDrops(plantInteract.getDropStorage(), event.getBlock());
            }
            // do other actions regardless of chance
            if (plantInteract.isTakeItem()) {
                decrementItemStack(itemStack);
            }
            // do interact actions for block
            if (!plantInteract.isOnlyEffectsOnChance() || chanceSuccess) {
                plantInteract.getEffectStorage().performEffects(event.getBlock(), event.getPlantBlock());
            }
            // do consumable actions
            if (consumable != null) {
                consumable.getEffectStorage().performEffects(event.getPlayer(), event.getPlantBlock());
            }
            // perform script block, if present
            if (plantInteract.getScriptBlock() != null) {
                plantInteract.getScriptBlock().loadValue(event.getPlantBlock(), event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlantConsume(PlantConsumeEvent event) {
        // check if player has permission
        if (!event.getPlayer().hasPermission("performantplants.consume")) {
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
                destroyPlantBlock(event.getBlock(), false);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                destroyPlantBlock(block, false);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // check if exploded blocks were Plants, and if so destroy them
        for (Block block : event.blockList()) {
            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                destroyPlantBlock(block, false);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (MetadataHelper.hasPlantBlockMetadata(event.getToBlock()) &&
                !event.getToBlock().getType().isSolid()) {
            Block block = event.getToBlock();
            destroyPlantBlock(block, true);
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
                destroyPlantBlock(block, true);
                // only destroy source block if it is a plant block
                if (MetadataHelper.hasPlantBlockMetadata(source)) {
                    destroyPlantBlock(source, true);
                }
            }
            event.setCancelled(true);
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
            boolean anyPlantBlocks = false;
            for (Block block : event.getBlocks()) {
                if (MetadataHelper.hasPlantBlockMetadata(block)) {
                    anyPlantBlocks = true;
                    break;
                }
            }
            if (anyPlantBlocks) {
                event.setCancelled(true);
                if (MetadataHelper.hasPlantBlockMetadata(event.getBlocks().get(0))) {
                    destroyPlantBlock(event.getBlocks().get(0), true);
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
            // check if sticky piston is retracting a sticky block
            boolean usingStickyBlock = false;
            if (event.getBlocks().size() > 0 &&
                    !MetadataHelper.hasPlantBlockMetadata(event.getBlocks().get(0)) && (
                            event.getBlocks().get(0).getBlockData().getMaterial() == Material.SLIME_BLOCK ||
                            event.getBlocks().get(0).getBlockData().getMaterial() == Material.HONEY_BLOCK
                            )) {
                usingStickyBlock = true;
            }
            // check if any pulled blocks were Plants
            for (Block block : event.getBlocks()) {
                // also check if the block was solid; non-solid can't get pulled
                if (MetadataHelper.hasPlantBlockMetadata(block) &&
                        block.getType().isSolid()) {
                    event.setCancelled(true);
                    // if was using sticky block, don't do anything now after cancellation
                    if (usingStickyBlock) {
                        return;
                    }
                    destroyPlantBlock(block, true);
                }
            }
        }
    }

    //endregion

    void destroyPlantBlock(Block block, PlantBlock plantBlock, boolean drops) {
        block.setType(Material.AIR);
        boolean removed = main.getPlantManager().removePlantBlock(plantBlock);
        // if block was not removed, don't do anything else
        if (!removed) {
            return;
        }
        // handle drops
        if (drops) {
            DropHelper.performDrops(plantBlock, block);
        }
        // if block's children should be removed, remove them
        if (plantBlock.isBreakChildren()) {
            ArrayList<BlockLocation> childLocations = new ArrayList<>(plantBlock.getChildLocations());
            for (BlockLocation childLocation : childLocations) {
                destroyPlantBlock(childLocation, drops);
            }
        }
        // if block's parent should be removed, remove it
        if (plantBlock.isBreakParent()) {
            BlockLocation parentLocation = plantBlock.getParentLocation();
            if (parentLocation != null) {
                destroyPlantBlock(parentLocation, drops);
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

    void decrementItemStack(ItemStack itemStack) {
        // if material is air, do nothing
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
    }

}
