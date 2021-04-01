package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantItemInteractEvent;
import me.kosinkadink.performantplants.events.PlantFarmlandTrampleEvent;
import me.kosinkadink.performantplants.events.PlantBlockInteractEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final PerformantPlants performantPlants;
    private final HashMap<UUID, Boolean> mainHandActionMap = new HashMap<>();
    private final HashMap<UUID, Boolean> dropActionMap = new HashMap<>();
    private final HashMap<UUID, Boolean> cancelOffhandMap = new HashMap<>();
    private final HashMap<UUID, Plant> expectedPlantMap = new HashMap<>();

    //region MainHandAction
    private boolean isMainHandAction(Player player) {
        Boolean action = mainHandActionMap.get(player.getUniqueId());
        if (action != null) {
            return action;
        }
        return false;
    }

    private void setMainHandAction(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND && player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            mainHandActionMap.put(player.getUniqueId(), true);
        } else {
            resetMainHandAction(player);
        }
    }

    private void resetMainHandAction(Player player) {
        mainHandActionMap.remove(player.getUniqueId());
    }
    //endregion
    //region DropAction
    private boolean isDropAction(Player player, Action action) {
        if (action == Action.LEFT_CLICK_AIR) {
            Boolean hasAction = dropActionMap.get(player.getUniqueId());
            if (hasAction != null) {
                return hasAction;
            }
        } else {
            resetDropAction(player);
        }
        return false;
    }

    public void setDropAction(Player player) {
        dropActionMap.put(player.getUniqueId(), true);
    }

    public void resetDropAction(Player player) {
        dropActionMap.remove(player.getUniqueId());
    }
    //endregion
    //region CancelOffhand
    private boolean isCancelOffhand(Player player) {
        Boolean cancelled = cancelOffhandMap.get(player.getUniqueId());
        if (cancelled != null) {
            return cancelled;
        }
        return false;
    }

    private void setCancelOffhand(Player player) {
        cancelOffhandMap.put(player.getUniqueId(), true);
    }

    private void resetCancelOffhand(Player player) {
        cancelOffhandMap.remove(player.getUniqueId());
    }
    //endregion
    //region ExpectPlant
    private Plant getExpectedPlant(Player player) {
        return expectedPlantMap.get(player.getUniqueId());
    }

    private void setExpectedPlant(Player player, Plant plant) {
        expectedPlantMap.put(player.getUniqueId(), plant);
    }

    private void resetExpectedPlant(Player player) {
        expectedPlantMap.remove(player.getUniqueId());
    }
    //endregion

    public PlayerInteractListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockAgainst().getType() == Material.CAMPFIRE || event.getBlockAgainst().getType() == Material.SOUL_CAMPFIRE) {
            if (performantPlants.getPlantTypeManager().isPlantItemStack(event.getItemInHand())) {
                event.setCancelled(true);
            }
        }
        // get plant expected to be placed
        Plant plant = getExpectedPlant(event.getPlayer());
        if (plant != null) {
            // cancel event
            event.setCancelled(true);
            // reset expected plant
            resetExpectedPlant(event.getPlayer());
            // check if allowed to place
            if (event.canBuild()) {
                performantPlants.getServer().getPluginManager().callEvent(
                        new PlantPlaceEvent(event.getPlayer(), plant, event.getBlockPlaced(), event.getBlockReplacedState(), event.getHand(), true)
                );
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getItem());
        // if plant item, cancel consume unless allowed
        if (plantItem != null && !plantItem.isAllowConsume()) {
            event.setCancelled(true);
            // create PlantConsumeEvent if is consumable by default consume
            if (plantItem.hasOnConsume()) {
                EquipmentSlot hand;
                if (event.getItem().isSimilar(event.getPlayer().getInventory().getItemInOffHand())) {
                    hand = EquipmentSlot.OFF_HAND;
                } else {
                    hand = EquipmentSlot.HAND;
                }
                ScriptBlock consumable = plantItem.getOnConsume();
                performantPlants.getServer().getPluginManager().callEvent(
                        new PlantItemInteractEvent(event.getPlayer(), consumable, hand)
                );
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        ItemStack itemStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            itemStack = event.getPlayer().getInventory().getItemInOffHand();
        } else {
            itemStack = event.getPlayer().getInventory().getItemInMainHand();
        }
        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
        // don't let plant items be used to feed animals/be interacted with entities, unless allowed
        if (plantItem != null && !plantItem.isAllowEntityInteract()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        if (!event.isCancelled()) {
            if (ItemHelper.isMaterialWearable(event.getPlayerItem())) {
                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getPlayerItem());
                if (plantItem != null && !plantItem.isAllowWear()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack;
        ItemStack otherStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            // interacting with offhand
            itemStack = player.getInventory().getItemInOffHand();
            // if off hand is empty, don't process off hand
            if (itemStack.getType() == Material.AIR) {
                resetMainHandAction(player);
                return;
            }
            // if offhand was cancelled, do nothing
            if (isCancelOffhand(player)) {
                event.setCancelled(true);
                resetCancelOffhand(player);
                resetMainHandAction(player);
                return;
            }
            otherStack = player.getInventory().getItemInMainHand();
            // if main hand not empty and already had an action, cancel offhand action and do nothing
            boolean isMainHandEmpty = otherStack.getType() == Material.AIR;
            if (!isMainHandEmpty && isMainHandAction(player)) {
                if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                    performantPlants.getLogger().info("Cancelling PlayerInteractEvent for Off Hand due to MainHandAction true");
                event.setCancelled(true);
                resetMainHandAction(player);
                return;
            } else {
                resetMainHandAction(player);
            }
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlayerInteractEvent for Off Hand");
        }
        else {
            // interacting with main hand
            resetExpectedPlant(event.getPlayer());
            resetCancelOffhand(player);
            if (isDropAction(player, event.getAction())) {
                event.setCancelled(true);
                resetDropAction(player);
                resetMainHandAction(player);
                return;
            }
            resetDropAction(player);
            itemStack = player.getInventory().getItemInMainHand();
            otherStack = player.getInventory().getItemInOffHand();
            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                performantPlants.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
        }
        Block block = event.getClickedBlock();
        // check if block is farmland; if so, got trampled
        if (event.getAction() == Action.PHYSICAL) {
            try {
                if (block.getType() == Material.FARMLAND) {
                    if (block.getWorld().getMaxHeight()-1 > block.getY()) {
                        // check if block above is a plant block
                        Block blockAbove = block.getWorld().getBlockAt(
                                block.getX(), block.getY() + 1, block.getZ()
                        );
                        if (MetadataHelper.hasPlantBlockMetadata(blockAbove)) {
                            // create PlantFarmlandTrampleEvent
                            PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(blockAbove);
                            // don't cancel event so the farmland will end up turning into dirt
                            if (plantBlock != null) {
                                performantPlants.getServer().getPluginManager().callEvent(
                                        new PlantFarmlandTrampleEvent(event.getPlayer(), plantBlock, blockAbove, block)
                                );
                                return;
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                // do nothing
            }
            return;
        }
        // region RIGHT CLICK BEHAVIOR
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // check if interacting with a plant block
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    block != null &&
                    MetadataHelper.hasPlantBlockMetadata(block)) {
                // get plant block interacted with
                PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(block);
                if (plantBlock != null && !player.isSneaking()) {
                    // only process main hand to avoid double interaction
                    if (event.getHand() == EquipmentSlot.HAND) {
                        // send out PlantInteractEvent
                        PlantBlockInteractEvent plantBlockInteractEvent = new PlantBlockInteractEvent(player, plantBlock, block, event.getBlockFace(), event.getHand());
                        performantPlants.getServer().getPluginManager().callEvent(plantBlockInteractEvent);
                        // cancel this event if plantInteractEvent was not cancelled
                        if (!plantBlockInteractEvent.isCancelled()) {
                            event.setCancelled(true);
                            setMainHandAction(player, event.getHand());
                            // cancel upcoming offhand action, if offhand does not contain air
                            if (!otherStack.getType().isAir()) {
                                setCancelOffhand(player);
                            }
                        }
                        // keep processing
                    }
                }
            }
            // check if trying to place down plant or consume plant item
            if (itemStack.getType() != Material.AIR) {
                Plant plant = performantPlants.getPlantTypeManager().getPlantByItemStack(itemStack);
                if (plant != null) {
                    PlantItem plantItem = null;
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                            block != null) {
                        // if block is inventory holder and player is sneaking, open block's inventory
                        if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
                            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                performantPlants.getLogger().info("Right clicked on campfire holding a plant item");
                            // cancel vanilla interaction if plant block
                            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                                event.setCancelled(true);
                            } else {
                                resetMainHandAction(player);
                                return;
                            }
                        }
                        if (BlockHelper.isInteractable(block) && !player.isSneaking()) {
                            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                performantPlants.getLogger().info("Prevented block from being placed on interactable block");
                            // cancel vanilla interaction if plant block
                            if (MetadataHelper.hasPlantBlockMetadata(block)) {
                                event.setCancelled(true);
                            } else {
                                resetMainHandAction(player);
                                return;
                            }
                        }
                        // check if item is consumable or player is sneaking
                        plantItem = plant.getItemByItemStack(itemStack);
                        if (!plantItem.hasOnRightClick() || player.isSneaking()) {
                            // check if item is a seed
                            if (plant.hasSeed() && plant.getSeedItemStack().isSimilar(itemStack)) {
                                // if enforce physics + is block OR not bypass space and is solid,
                                // use BlockPlaceEvent to handle placement
                                if ((plant.isEnforcePhysics() && itemStack.getType().isBlock()) || (!plant.isBypassSpace() && itemStack.getType().isSolid())) {
                                    // set plant the BlockPlaceEvent handler should expect when seeing this player
                                    setExpectedPlant(player, plant);
                                }
                                // otherwise, send out PlantBlockEvent here
                                else {
                                    // cancel event and send out PlantBlockEvent
                                    event.setCancelled(true);
                                    setMainHandAction(player, event.getHand());
                                    performantPlants.getServer().getPluginManager().callEvent(
                                            new PlantPlaceEvent(player, plant, block.getRelative(event.getBlockFace()), event.getHand(), true)
                                    );
                                }
                                // return here either way
                                return;
                            }
                            // cancel event unless plant item can and is allowed to be worn, or is edible
                            if (!(itemStack.getType().isEdible() || ItemHelper.isMaterialWearableWithRightClick(plantItem.getItemStack()) && plantItem.isAllowWear())) {
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                            performantPlants.getLogger().info("Block was NULL");
                    }
                    // check if plant item can be consumed
                    if (plantItem == null) {
                        plantItem = plant.getItemByItemStack(itemStack);
                    }
                    if (plantItem.hasOnRightClick()) {
                        // if in offhand and main hand is consumable, don't do anything
                        boolean performConsumeForThisHand = true;
                        if (event.getHand() == EquipmentSlot.OFF_HAND) {
                            PlantItem otherItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                            if (otherItem != null && otherItem.hasOnRightClick()) {
                                performConsumeForThisHand = false;
                            }
                        }
                        if (performConsumeForThisHand) {
                            if (!itemStack.getType().isEdible()) {
                                event.setCancelled(true);
                            }
                            PlantItemInteractEvent plantItemInteractEvent = new PlantItemInteractEvent(player, plantItem.getOnRightClick(), event.getHand());
                            performantPlants.getServer().getPluginManager().callEvent(plantItemInteractEvent);

                            if (!plantItemInteractEvent.isCancelled()) {
                                setMainHandAction(player, event.getHand());
                                return;
                            }
                        }
                    }
                    // cancel armor equip if material can be worn but not allowed to
                    if (ItemHelper.isMaterialWearableWithRightClick(plantItem.getItemStack()) && !plantItem.isAllowWear()) {
                        event.setCancelled(true);
                    }
                } else {
                    // check if item in other hand is consumable
                    if (!otherStack.getType().isAir() && !itemStack.getType().isEdible() && !performantPlants.getPlantTypeManager().isPlantItemStack(itemStack)) {
                        PlantItem otherItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                        if (otherItem != null && otherItem.hasOnRightClick()) {
                            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                performantPlants.getLogger().info("Prevented required block for consumable to perform its own action");
                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
                                if (block != null && BlockHelper.isInteractable(block) && !player.isSneaking()) {
                                    if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                        performantPlants.getLogger().info("Prevented plant consume from overriding interactable block");
                                    resetMainHandAction(player);
                                    return;
                                }
                                ScriptBlock otherConsumable = otherItem.getOnRightClick();
                                PlantItemInteractEvent plantItemInteractEvent = new PlantItemInteractEvent(player, otherConsumable, EquipmentSlot.OFF_HAND);
                                performantPlants.getServer().getPluginManager().callEvent(plantItemInteractEvent);
                                if (!plantItemInteractEvent.isCancelled()) {
                                    event.setCancelled(true);
                                    resetMainHandAction(player);
                                    return;
                                }
                            }
                        }
                    }
                    if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                        performantPlants.getLogger().info("Plant was NULL, doing nothing");
                }
            resetMainHandAction(player);
            return;
            }
        }
        // endregion

        // region LEFT CLICK BEHAVIOR
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            // check if clicking on a plant block
            if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
                    block != null &&
                    MetadataHelper.hasPlantBlockMetadata(block)) {
                // get plant block interacted with
                PlantBlock plantBlock = performantPlants.getPlantManager().getPlantBlock(block);
                if (plantBlock != null) {
                    PlantBlockInteractEvent plantBlockInteractEvent = new PlantBlockInteractEvent(player, plantBlock, block, event.getBlockFace(), event.getHand(), true);
                    performantPlants.getServer().getPluginManager().callEvent(
                            plantBlockInteractEvent
                    );
                    return;
                }
            }
            // check if there is any general or vanilla click behavior
            if (itemStack.getType() != Material.AIR) {
                Plant plant = performantPlants.getPlantTypeManager().getPlantByItemStack(itemStack);
                if (plant != null) {
                    PlantItem plantItem;
                    plantItem = plant.getItemByItemStack(itemStack);
                    if (plantItem.hasOnLeftClick()) {
                        ScriptBlock consumable = plantItem.getOnLeftClick();
                        PlantItemInteractEvent plantItemInteractEvent = new PlantItemInteractEvent(player, consumable, event.getHand());
                        performantPlants.getServer().getPluginManager().callEvent(plantItemInteractEvent);
                    }
                }
            }
        }
        // endregion
    }
}
