package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.scripting.ExecutionContext;
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

    private final PerformantPlants performantPlants;

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
            ExecutionContext context = new ExecutionContext().set(player);
            PlantInteract interact = storage.getPlantInteract(heldItem, context, null);
            if (interact != null) {
                // perform drops
                DropHelper.performDrops(interact.getDropStorage(), event.getEntity().getLocation(), context);
                // perform effects
                context.setPlantBlock(PlantBlock.wrapBlock(event.getEntity().getLocation().getBlock()));
                interact.getEffectStorage().performEffectsBlock(context);
                // perform consumable, if killer is not null
                if (player != null && interact.getConsumableStorage() != null ) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(context, EquipmentSlot.HAND);
                    if (consumable != null) {
                        consumable.getEffectStorage().performEffectsPlayer(context);
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
            ExecutionContext context = new ExecutionContext()
                    .set(player)
                    .set(PlantBlock.wrapBlock(block));
            PlantInteract interact = storage.getPlantInteract(heldItem, context, null);
            if (interact != null) {
                // perform drops
                DropHelper.performDrops(interact.getDropStorage(), block.getLocation(), context);
                // perform effects
                interact.getEffectStorage().performEffectsBlock(context);
                // perform consumable, if killer is not null
                if (interact.getConsumableStorage() != null ) {
                    PlantConsumable consumable = interact.getConsumableStorage().getConsumable(context, EquipmentSlot.HAND);
                    if (consumable != null) {
                        consumable.getEffectStorage().performEffectsPlayer(context);
                    }
                }
            }
        }
    }

}
