package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.Main;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.builders.PlantItemBuilder;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.plants.PlantRecipe;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

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
    public void onCraftItem(CraftItemEvent event) {
        if (!event.isCancelled()) {
            PlantRecipe plantRecipe = main.getRecipeManager().getRecipe(event.getRecipe());
            if (plantRecipe == null) {
                return;
            }
            PlantInteractStorage storage = plantRecipe.getStorage();
            if (storage != null) {
                PlantInteract interact = storage.getPlantInteract(event.getWhoClicked().getInventory().getItemInMainHand());
                if (interact != null) {
                    if (plantRecipe.isIgnoreResult()) {
                        event.setCurrentItem(new ItemBuilder(Material.AIR).build());
                        // removing current item causes ingredients not to be consumed
                        // so consume them manually
                        for (ItemStack itemStack : event.getInventory().getMatrix()) {
                            if (itemStack != null && itemStack.getType() != Material.AIR) {
                                itemStack.setAmount(itemStack.getAmount()-1);
                            }
                        }
                    }
                    // perform effects on block
                    try {
                        interact.getEffectStorage().performEffects(event.getInventory().getLocation().getBlock());
                    } catch (NullPointerException e) {
                        // do nothing, just catch
                    }
                    // perform effects on player, if any
                    PlantConsumableStorage consumableStorage = interact.getConsumableStorage();
                    if (consumableStorage != null) {
                        Player player = (Player) event.getWhoClicked();
                        PlantConsumable consumable = consumableStorage.getConsumable(player, EquipmentSlot.HAND);
                        if (consumable != null) {
                            consumable.getEffectStorage().performEffects(player, player.getLocation());
                        }
                    }
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
