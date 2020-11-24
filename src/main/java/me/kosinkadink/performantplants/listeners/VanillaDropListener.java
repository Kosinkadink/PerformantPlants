package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.MetadataHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class VanillaDropListener implements Listener {

    private PerformantPlants performantPlants;

    public VanillaDropListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        PlantInteractStorage storage = performantPlants.getVanillaDropManager().getInteract(event.getEntityType());
        if (storage != null) {
            Player player = event.getEntity().getKiller();
            ItemStack heldItem;
            if (player == null) {
                heldItem = new ItemBuilder(Material.AIR).build();
            } else {
                heldItem = player.getInventory().getItemInMainHand();
            }
            PlantInteract interact = storage.getPlantInteract(heldItem);
            if (interact != null) {
                // perform drops
                DropHelper.performDrops(interact.getDropStorage(), event.getEntity().getLocation(), player, null);
                // perform effects
                interact.getEffectStorage().performEffects(event.getEntity().getLocation().getBlock(), null);
                // perform consumable, if killer is not null
                if (player != null && interact.getConsumableStorage() != null ) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(player, EquipmentSlot.HAND);
                    if (consumable != null) {
                        consumable.getEffectStorage().performEffects(player, null);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // do nothing if in creative
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        // do nothing if is a plant block
        if (MetadataHelper.hasPlantBlockMetadata(event.getBlock())) {
            return;
        }
        Block block = event.getBlock();
        // otherwise, check if have custom drops
        PlantInteractStorage storage = performantPlants.getVanillaDropManager().getInteract(block);
        if (storage != null) {
            Player player = event.getPlayer();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            PlantInteract interact = storage.getPlantInteract(heldItem);
            if (interact != null) {
                // perform drops
                DropHelper.performDrops(interact.getDropStorage(), block, player, PlantBlock.wrapBlock(block));
                // perform effects
                interact.getEffectStorage().performEffects(block, PlantBlock.wrapBlock(block));
                // perform consumable, if killer is not null
                if (interact.getConsumableStorage() != null ) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(player, EquipmentSlot.HAND);
                    if (consumable != null) {
                        consumable.getEffectStorage().performEffects(player, PlantBlock.wrapBlock(block));
                    }
                }
            }
        }
    }

}
