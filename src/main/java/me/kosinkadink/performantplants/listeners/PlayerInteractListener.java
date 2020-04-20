package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.events.PlantConsumeEvent;
import me.kosinkadink.performantplants.events.PlantFarmlandTrampleEvent;
import me.kosinkadink.performantplants.events.PlantInteractEvent;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private Main main;

    public PlayerInteractListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockAgainst().getType() == Material.CAMPFIRE) {
            if (main.getPlantTypeManager().getPlantByItemStack(event.getItemInHand()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            // interacting with offhand
            itemStack = player.getInventory().getItemInOffHand();
            // if off hand is empty, don't process off hand
            if (itemStack.getType() == Material.AIR) {
                return;
            }
            main.getLogger().info("Reviewing PlayerInteractEvent for Off Hand");
        }
        else {
            // interacting with main hand
            itemStack = player.getInventory().getItemInMainHand();
            main.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
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
            if (plantBlock != null) {
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
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        block != null) {
                    // if block is inventory holder and player is sneaking, open block's inventory
                    if (block.getState() instanceof InventoryHolder && !player.isSneaking()) {
                        event.setCancelled(true);
                        player.openInventory(((InventoryHolder) block.getState()).getInventory());
                        main.getLogger().info("Prevented block from being placed on InventoryHolder block");
                        return;
                    }
                    if (block.getType() == Material.CAMPFIRE) {
                        main.getLogger().info("Right clicked on campfire holding a plant item");
                        return;
                    }
                    // TODO: decide if should make exception for blocks that should be interactable while holding plant
                    //  item
                    // if interactable block, let action go through if not sneaking
//                    if (!player.isSneaking() && (block.getType() == Material.ANVIL ||
//                            block.getType() == Material.CHIPPED_ANVIL || block.getType() == Material.DAMAGED_ANVIL ||
//                            block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.STONECUTTER ||
//                            block.getType() == Material.CRAFTING_TABLE)) {
//                        main.getLogger().info("Prevented block from being placed on interactable block");
//                        return;
//                    }
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
                // check if plant item can be consumed
                PlantItem plantItem = plant.getItemByItemStack(itemStack);
                if (plantItem.isConsumable()) {
                    event.setCancelled(true);
                    main.getServer().getPluginManager().callEvent(
                            new PlantConsumeEvent(player, plantItem, event.getHand())
                    );
                }
                main.getLogger().info("Action was not RIGHT CLICK or block was NULL");
            }
            else {
                main.getLogger().info("Plant was NULL, doing nothing");
            }
        }
    }
}
