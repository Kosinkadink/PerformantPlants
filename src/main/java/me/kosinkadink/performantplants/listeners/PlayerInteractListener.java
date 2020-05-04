package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.events.PlantConsumeEvent;
import me.kosinkadink.performantplants.events.PlantFarmlandTrampleEvent;
import me.kosinkadink.performantplants.events.PlantInteractEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.plants.RequiredItem;
import me.kosinkadink.performantplants.util.BlockHelper;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import me.kosinkadink.performantplants.util.PlayerHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private Main main;

    public PlayerInteractListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockAgainst().getType() == Material.CAMPFIRE) {
            if (PlantItemBuilder.isPlantName(event.getItemInHand())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        PlantItem plantItem = main.getPlantTypeManager().getPlantItemByItemStack(event.getItem());
        if (plantItem != null) {
            // create PlantConsumeEvent if is consumable by default consume
            event.setCancelled(true);
            if (plantItem.getConsumableStorage() != null) {
                EquipmentSlot hand;
                if (event.getItem().isSimilar(event.getPlayer().getInventory().getItemInOffHand())) {
                    hand = EquipmentSlot.OFF_HAND;
                } else {
                    hand = EquipmentSlot.HAND;
                }
                PlantConsumable consumable = plantItem.getConsumableStorage().getConsumable(event.getPlayer(), hand);
                if (consumable != null && consumable.isNormalEat()) {
                    main.getServer().getPluginManager().callEvent(
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
        PlantItem plantItem = main.getPlantTypeManager().getPlantItemByItemStack(itemStack);
        // don't let plant items be used to feed animals/be interacted with entities
        if (plantItem != null) {
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
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlayerInteractEvent for Off Hand");
        }
        else {
            // interacting with main hand
            itemStack = player.getInventory().getItemInMainHand();
            otherStack = player.getInventory().getItemInOffHand();
            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
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
                            PlantBlock plantBlock = main.getPlantManager().getPlantBlock(blockAbove);
                            // don't cancel event so the farmland will end up turning into dirt
                            if (plantBlock != null) {
                                main.getServer().getPluginManager().callEvent(
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
        // check if interacting with a plant block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                block != null &&
                MetadataHelper.hasPlantBlockMetadata(block)) {
            // get plant block interacted with
            PlantBlock plantBlock = main.getPlantManager().getPlantBlock(block);
            if (plantBlock != null && !player.isSneaking()) {
                // cancel event and send out PlantInteractEvent ONLY IF main hand to avoid double interaction
                event.setCancelled(true);
                main.getServer().getPluginManager().callEvent(
                        new PlantInteractEvent(player, plantBlock, block, event.getHand())
                );
                return;
            }
        }
        // check if trying to place down plant or consume plant item
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) &&
                itemStack.getType() != Material.AIR) {
            Plant plant = main.getPlantTypeManager().getPlantByItemStack(itemStack);
            if (plant != null) {
                PlantItem plantItem;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        block != null) {
                    // if block is inventory holder and player is sneaking, open block's inventory
                    if (block.getType() == Material.CAMPFIRE) {
                        if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Right clicked on campfire holding a plant item");
                        return;
                    }
                    if (BlockHelper.isInteractable(block) && !player.isSneaking()) {
                        if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Prevented block from being placed on interactable block");
                        return;
                    }
                    // check if item is consumable or player is sneaking
                    plantItem = plant.getItemByItemStack(itemStack);
                    if (!plantItem.isConsumable() || player.isSneaking()) {
                        // check if item is a seed (cancel event regardless)
                        event.setCancelled(true);
                        if (plant.hasSeed() && plant.getSeedItemStack().isSimilar(itemStack)) {
                            // cancel event and send out PlantBlockEvent
                            main.getServer().getPluginManager().callEvent(
                                    new PlantPlaceEvent(player, plant, block.getRelative(event.getBlockFace()), event.getHand(), true)
                            );
                            return;
                        }
                    }
                }
                // check if plant item can be consumed
                plantItem = plant.getItemByItemStack(itemStack);
                if (plantItem.isConsumable()) {
                    // if in offhand and main hand is consumable, don't do anything
                    boolean performConsumeForThisHand = true;
                    if (event.getHand() == EquipmentSlot.OFF_HAND) {
                        PlantItem otherItem = main.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                        if (otherItem != null && otherItem.isConsumable()) {
                            performConsumeForThisHand = false;
                        }
                    }
                    if (performConsumeForThisHand) {
                        PlantConsumable consumable = plantItem.getConsumableStorage().getConsumable(player, event.getHand());
                        if (!plantItem.getItemStack().getType().isEdible() || (consumable != null && !consumable.isNormalEat())) {
                            event.setCancelled(true);
                            main.getServer().getPluginManager().callEvent(
                                    new PlantConsumeEvent(player, consumable, event.getHand())
                            );
                        }
                    }
                }
                if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Action was not RIGHT CLICK or block was NULL");
            }
            else {
                // check if item in other hand is consumable
                if (!otherStack.getType().isAir() && !PlantItemBuilder.isPlantName(itemStack) && !itemStack.getType().isEdible()) {
                    PlantItem otherItem = main.getPlantTypeManager().getPlantItemByItemStack(otherStack);
                    if (otherItem != null && otherItem.isConsumable()) {
                        PlantConsumable otherConsumable = otherItem.getConsumableStorage().getConsumable(player,
                                PlayerHelper.oppositeHand(event.getHand()));
                        if (otherConsumable != null) {
                            event.setCancelled(true);
                            if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Prevented required block for consumable to perform its own action");
                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
                                main.getServer().getPluginManager().callEvent(
                                        new PlantConsumeEvent(player, otherConsumable, EquipmentSlot.OFF_HAND)
                                );
                            }
                            return;
                        }
                    }
                }
                if (main.getConfigManager().getConfigSettings().isDebug()) main.getLogger().info("Plant was NULL, doing nothing");
            }
        }
    }
}
