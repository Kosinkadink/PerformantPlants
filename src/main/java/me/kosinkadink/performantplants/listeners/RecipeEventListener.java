package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.plants.Plant;
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
                if (main.getPlantTypeManager().getPlantByItemStack(ingredient) != null) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Plant plant = main.getPlantTypeManager().getPlantByItemStack(event.getSource());
        // check if a smelting recipe was registered for item stack
        if (plant != null && !main.getRecipeManager().isInputForFurnaceRecipe(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        // check if a plant item is about to be used as fuel
        Plant plant = main.getPlantTypeManager().getPlantByItemStack(event.getFuel());
        // cancel if plant
        if (plant != null) {
            event.setCancelled(true);
            return;
        }
        // check if a plant item is trying to be cooked
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        FurnaceInventory furnaceInventory = (FurnaceInventory) inventoryHolder.getInventory();
        plant = main.getPlantTypeManager().getPlantByItemStack(furnaceInventory.getSmelting());
        // check if a smelting recipe was registered for item stack
        if (plant != null && !main.getRecipeManager().isInputForFurnaceRecipe(furnaceInventory.getSmelting())) {
            event.setCancelled(true);
        }
    }

}
