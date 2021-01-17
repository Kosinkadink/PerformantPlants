package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantItem;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class InventoryEventListener implements Listener {

    private final PerformantPlants performantPlants;

    public InventoryEventListener(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!event.isCancelled()) {
            if (event.getClickedInventory() != null) {
                // check which inventory type
                switch (event.getClickedInventory().getType()) {
                    case GRINDSTONE:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            // check if trying to use plant item
                            for (ItemStack itemStack : event.getClickedInventory().getStorageContents()) {
                                // if plant item, check if allowed to used in this inventory
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                                if (plantItem != null && !plantItem.isAllowGrindstone()) {
                                    event.setResult(Event.Result.DENY);
                                    break;
                                }
                            }
                        } break;
                    case STONECUTTER:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            // check if trying to use plant item
                            for (ItemStack itemStack : event.getClickedInventory().getStorageContents()) {
                                // if plant item, check if allowed to used in this inventory
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                                if (plantItem != null && !plantItem.isAllowStonecutter()) {
                                    event.setResult(Event.Result.DENY);
                                    break;
                                }
                            }
                        } break;
                    case LOOM:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            // check if trying to use plant item
                            for (ItemStack itemStack : event.getClickedInventory().getStorageContents()) {
                                // if plant item, check if allowed to used in this inventory
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                                if (plantItem != null && !plantItem.isAllowLoom()) {
                                    event.setResult(Event.Result.DENY);
                                    break;
                                }
                            }
                        } break;
                    case CARTOGRAPHY:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            // check if trying to use plant item
                            for (ItemStack itemStack : event.getClickedInventory().getStorageContents()) {
                                // if plant item, check if allowed to used in this inventory
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                                if (plantItem != null && !plantItem.isAllowCartography()) {
                                    event.setResult(Event.Result.DENY);
                                    break;
                                }
                            }
                        } break;
                }
            }
        }
    }

}
