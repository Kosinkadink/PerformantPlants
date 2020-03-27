package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private Main main;

    public PlayerInteractListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //boolean cancelled = event.isCancelled();
        Player player = event.getPlayer();
        ItemStack itemStack;
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            // TODO: handle interacting with offhand
            itemStack = player.getInventory().getItemInOffHand();
            main.getLogger().info("Reviewing PlayerInteractEvent for Off Hand");
        }
        else {
            itemStack = player.getInventory().getItemInMainHand();
            main.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
        }
        Block block = event.getClickedBlock();
        // check if interacting with a plant block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                block != null &&
                MetadataHelper.hasPlantBlockMetadata(block)) {
            // TODO: handle interacting with plants
            Plant plant = main.getPlantTypeManager().getPlantPlacedWith(itemStack);
            if (plant != null) {
                event.setCancelled(true);
                main.getLogger().info("Prevented plant block from being placed when right clicking plant block");
                return;
            }
            event.setCancelled(true);
            main.getLogger().info("Prevented vanilla block from being placed when right clicking plant block");
            return;
        }
        // check if trying to place down plant
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                itemStack.getType() != Material.AIR) {
            Plant plant = main.getPlantTypeManager().getPlantPlacedWith(itemStack);
            if (plant != null) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        block != null) {
                    // if plant's item not placeable, cancel event
                    if (!plant.isPlaceable() && plant.getItem().isSimilar(itemStack)) {
                        event.setCancelled(true);
                        main.getLogger().info("Prevented unplaceable plant block from being placed");
                        return;
                    }
                    // if item is plant's seed, set grows to true
                    boolean grows = false;
                    if (plant.hasSeed() && plant.getSeedItem().isSimilar(itemStack)) {
                        grows = true;
                    }
                    // cancel event and send out PlantBlockEvent
                    event.setCancelled(true);
                    main.getServer().getPluginManager().callEvent(
                            new PlantPlaceEvent(player, plant, block.getRelative(event.getBlockFace()), grows)
                    );
                }
                else {
                    main.getLogger().info("Action was not RIGHT CLICK or block was NULL");
                }
            }
            else {
                main.getLogger().info("Plant was NULL, doing nothing");
            }
        }
    }
}
