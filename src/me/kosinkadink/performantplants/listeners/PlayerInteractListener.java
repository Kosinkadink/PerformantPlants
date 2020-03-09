package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.events.PlantPlaceEvent;
import me.kosinkadink.performantplants.plants.Plant;
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
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            // TODO: handle interacting with offhand
            return;
        }
        main.getLogger().info("Reviewing PlayerInteractEvent for Main Hand");
        Block block = event.getClickedBlock();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        // check if interacting with a plant block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                block != null &&
                block.hasMetadata("performantplants-plant")) {
            // TODO: handle interacting with plants
            return;
        }
        // check if trying to place down plant
        if (itemStack.getType() != Material.AIR) {
            Plant plant = main.getPlantTypeManager().getPlant(itemStack);
            if (plant != null) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        block != null) {
                    // cancel event and send out PlantBlockEvent
                    event.setCancelled(true);
                    main.getServer().getPluginManager().callEvent(
                            new PlantPlaceEvent(player, plant, block.getRelative(event.getBlockFace()))
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
