package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.RecipeCheckResult;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.RecipeHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class InventoryEventListener implements Listener {

    private final PerformantPlants performantPlants;

    public InventoryEventListener(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        // completely block interaction with result
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

    @EventHandler
    public void onResultInventoryClickEvent(InventoryClickEvent event) {
        // check that result is valid
        if (!event.isCancelled()) {
            if (event.getClickedInventory() != null) {
                // check which inventory type
                switch (event.getClickedInventory().getType()) {
                    case ANVIL:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            AnvilInventory inventory = (AnvilInventory) event.getClickedInventory();
                            // check recipe result
                            RecipeCheckResult checkResult = RecipeHelper.checkAnvilRecipe(inventory);
                            // if null, do nothing and let behavior play out
                            if (checkResult != null) {
                                // if allowed, decrement base and addition item stacks (if more than one)
                                if (checkResult.isAllow()) {
                                    // decrement base and give back to player
                                    ItemStack base = inventory.getItem(0);
                                    if (base != null && base.getAmount() > 1) {
                                        base.setAmount(base.getAmount()-1);
                                        performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                                                DropHelper.givePlayerItemStack((Player) event.getWhoClicked(), base)
                                        );
                                    }
                                    // decrement addition and give back to player
                                    ItemStack addition = inventory.getItem(1);
                                    if (addition != null && addition.getAmount() > 1) {
                                        addition.setAmount(addition.getAmount()-1);
                                        performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                                                DropHelper.givePlayerItemStack((Player) event.getWhoClicked(), addition)
                                        );
                                    }
                                    // NOTE: giving the items is done through scheduler to avoid client-side visual bug
                                    // where, if one of the ingredients and result are the same, the result would appear
                                    // to be duplicated.
                                    // TODO: perform actions on allow
                                } else {
                                    event.setResult(Event.Result.DENY);
                                }
                            }
                        } break;
                    case SMITHING:
                        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                            SmithingInventory inventory = (SmithingInventory) event.getClickedInventory();
                            // check recipe result
                            RecipeCheckResult checkResult = RecipeHelper.checkSmithingRecipe(inventory);
                            // if null, do nothing and let behavior play out
                            if (checkResult != null) {
                                // if not allowed, intervene
                                if (!checkResult.isAllow()) {
                                    // deny and set result to air
                                    event.setResult(Event.Result.DENY);
                                    event.setCancelled(true);
                                }
                                // TODO: perform actions on allow
                            }
                        } break;

                }
            }
        }
    }

}
