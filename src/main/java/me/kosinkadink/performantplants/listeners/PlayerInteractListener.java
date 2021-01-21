package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantConsumeEvent;
import me.kosinkadink.performantplants.events.PlantFarmlandTrampleEvent;
import me.kosinkadink.performantplants.events.PlantInteractEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final PerformantPlants performantPlants;

    public PlayerInteractListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockAgainst().getType() == Material.CAMPFIRE) {
            if (performantPlants.getPlantTypeManager().isPlantItemStack(event.getItemInHand())) {
                event.setCancelled(true);
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
            if (plantItem.getConsumableStorage() != null) {
                EquipmentSlot hand;
                if (event.getItem().isSimilar(event.getPlayer().getInventory().getItemInOffHand())) {
                    hand = EquipmentSlot.OFF_HAND;
                } else {
                    hand = EquipmentSlot.HAND;
                }
                PlantConsumable consumable = plantItem.getConsumableStorage().getConsumable(event.getPlayer(), hand);
                if (consumable != null && consumable.isNormalEat(event.getPlayer(), null)) {
                    performantPlants.getServer().getPluginManager().callEvent(
                            new PlantConsumeEvent(event.getPlayer(), consumable, hand)
                    );
                }
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack;
        ItemStack otherStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            // interacting with offhand
            itemStack = player.getInventory().getItemInOffHand();
            // if off hand is empty, don't process off hand
            if (itemStack.getType() == Material.AIR) {
                return;
            }
            otherStack = player.getInventory().getItemInMainHand();
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Reviewing PlayerInteractEvent for Off Hand");
        }
        else {
            // interacting with main hand
            itemStack = player.getInventory().getItemInMainHand();
            otherStack = player.getInventory().getItemInOffHand();
            if (performantPlants.getConfigManager().getConfigSettings().isDebug()) performantPlants.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
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
                        PlantInteractEvent plantInteractEvent = new PlantInteractEvent(player, plantBlock, block, event.getBlockFace(), event.getHand());
                        performantPlants.getServer().getPluginManager().callEvent(plantInteractEvent);
                        // cancel this event if plantInteractEvent was not cancelled
                        if (!plantInteractEvent.isCancelled()) {
                            event.setCancelled(true);
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
                        if (block.getType() == Material.CAMPFIRE) {
                            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                performantPlants.getLogger().info("Right clicked on campfire holding a plant item");
                            return;
                        }
                        if (BlockHelper.isInteractable(block) && !player.isSneaking()) {
                            if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                performantPlants.getLogger().info("Prevented block from being placed on interactable block");
                            return;
                        }
                        // check if item is consumable or player is sneaking
                        plantItem = plant.getItemByItemStack(itemStack);
                        if (!plantItem.isConsumable() || player.isSneaking()) {
                            // check if item is a seed (cancel event regardless)
                            event.setCancelled(true);
                            if (plant.hasSeed() && plant.getSeedItemStack().isSimilar(itemStack)) {
                                // cancel event and send out PlantBlockEvent
                                performantPlants.getServer().getPluginManager().callEvent(
                                        new PlantPlaceEvent(player, plant, block.getRelative(event.getBlockFace()), event.getHand(), true)
                                );
                                return;
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
                    if (plantItem.isConsumable()) {
                        // if in offhand and main hand is consumable, don't do anything
                        boolean performConsumeForThisHand = true;
                        if (event.getHand() == EquipmentSlot.OFF_HAND) {
                            PlantItem otherItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                            if (otherItem != null && otherItem.isConsumable()) {
                                performConsumeForThisHand = false;
                            }
                        }
                        if (performConsumeForThisHand) {
                            PlantConsumable consumable = plantItem.getConsumableStorage().getConsumable(player, event.getHand());
                            if (!plantItem.getItemStack().getType().isEdible() || (consumable != null && !consumable.isNormalEat(player, null))) {
                                event.setCancelled(true);
                                performantPlants.getServer().getPluginManager().callEvent(
                                        new PlantConsumeEvent(player, consumable, event.getHand())
                                );
                                return;
                            }
                        }
                    }
                } else {
                    // check if item in other hand is consumable
                    if (!otherStack.getType().isAir() && !itemStack.getType().isEdible() && !performantPlants.getPlantTypeManager().isPlantItemStack(itemStack)) {
                        PlantItem otherItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                        if (otherItem != null && otherItem.isConsumable()) {
                            PlantConsumable otherConsumable = otherItem.getConsumableStorage().getConsumable(player,
                                    PlayerHelper.oppositeHand(event.getHand()));
                            if (otherConsumable != null) {
                                event.setCancelled(true);
                                if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                                    performantPlants.getLogger().info("Prevented required block for consumable to perform its own action");
                                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
                                    performantPlants.getServer().getPluginManager().callEvent(
                                            new PlantConsumeEvent(player, otherConsumable, EquipmentSlot.OFF_HAND)
                                    );
                                }
                                return;
                            }
                        }
                    }
                    if (performantPlants.getConfigManager().getConfigSettings().isDebug())
                        performantPlants.getLogger().info("Plant was NULL, doing nothing");
                }
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
                    PlantInteractEvent plantInteractEvent = new PlantInteractEvent(player, plantBlock, block, event.getBlockFace(), event.getHand(), true);
                    performantPlants.getServer().getPluginManager().callEvent(
                            plantInteractEvent
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
                    if (plantItem.isClickable()) {
                        PlantConsumable consumable = plantItem.getClickableStorage().getConsumable(player, event.getHand());
                        if (consumable != null) {
                            performantPlants.getServer().getPluginManager().callEvent(
                                    new PlantConsumeEvent(player, consumable, event.getHand())
                            );
                        }
                    }
                }
            }
        }
        // endregion
    }
}
