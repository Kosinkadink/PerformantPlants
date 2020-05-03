package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class RecipeEventListener implements Listener {

    private Main main;

    public RecipeEventListener(Main mainClass) {
        main = mainClass;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        // check if a vanilla recipe is trying to use a plant item
        if (!main.getRecipeManager().isRecipe(event.getRecipe())) {
            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                // if plant item is used, set result to air
                if (PlantItemBuilder.isPlantName(ingredient)) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        // check if a smelting recipe was registered for item stack
        if (PlantItemBuilder.isPlantName(event.getSource())) {
            switch (event.getBlock().getType()) {
                case FURNACE:
                    if (!main.getRecipeManager().isInputForFurnaceRecipe(event.getSource())) {
                        event.setCancelled(true);
                    }
                    break;
                case BLAST_FURNACE:
                    if (!main.getRecipeManager().isInputForBlastingRecipe(event.getSource())) {
                        event.setCancelled(true);
                    }
                    break;
                case SMOKER:
                    if (!main.getRecipeManager().isInputForSmokingRecipe(event.getSource())) {
                        event.setCancelled(true);
                    }
                    break;
                case CAMPFIRE:
                    if (!main.getRecipeManager().isInputForCampfireRecipe(event.getSource())) {
                        event.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        // check if a plant item is about to be used as fuel
        // cancel if plant
        if (PlantItemBuilder.isPlantName(event.getFuel())) {
            event.setCancelled(true);
            return;
        }
        // check if a plant item is trying to be cooked
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        FurnaceInventory furnaceInventory = (FurnaceInventory) inventoryHolder.getInventory();
        // check if a smelting recipe was registered for item stack
        if (PlantItemBuilder.isPlantName(furnaceInventory.getSmelting()) && !main.getRecipeManager().isInputForFurnaceRecipe(furnaceInventory.getSmelting())) {
            event.setCancelled(true);
        }
    }

}
