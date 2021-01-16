package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
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
            // if AnvilInventory, check if trying to rename or use plant item
            if (event.getClickedInventory() instanceof AnvilInventory) {
                if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                    // check all item stacks involved to get anvil result
                    for (ItemStack itemStack : event.getClickedInventory()) {
                        // if item stack is a plant item, cancel event and stop searching for more
                        if (performantPlants.getPlantTypeManager().isPlantItemStack(itemStack)) {
                            event.setResult(Event.Result.DENY);
                            break;
                        }
                    }
                }
            }
        }
    }

}
