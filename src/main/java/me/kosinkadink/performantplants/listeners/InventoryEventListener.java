package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.RecipeCheckResult;
import me.kosinkadink.performantplants.util.DropHelper;
import me.kosinkadink.performantplants.util.ItemHelper;
import me.kosinkadink.performantplants.util.RecipeHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.Collection;
import java.util.Map;

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
                                    // deny
                                    event.setResult(Event.Result.DENY);
                                }
                                // TODO: perform actions on allow
                            }
                        } break;

                }
            }
        }
    }

    //region Beacon Inventory Cancelling
    @EventHandler
    public void onBeaconInventoryClickEvent(InventoryClickEvent event) {
        if (!event.isCancelled()) {
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) {
                return;
            }
            switch (clickedInventory.getType()) {
                case BEACON:
                    // if shift click, do nothing (item being removed from beacon)
                    if (event.isShiftClick()) {
                        return;
                    }
                    ItemStack content = event.getCurrentItem();
                    ItemStack cursorItemStack = event.getCursor();
                    // if nothing on cursor, then nothing would be placed in beacon
                    if (cursorItemStack == null || cursorItemStack.getType() == Material.AIR) {
                        return;
                    }
                    // if nothing in slot, then see if holding something that could be placed in
                    if (content == null || content.getType() == Material.AIR) {
                        // if not allowed, deny
                        if (isNotAllowedInBeacon(cursorItemStack)) {
                            event.setResult(Event.Result.DENY);
                            return;
                        }
                    }
                    else {
                        // check that a single item on cursor (only replaces content if single)
                        if (cursorItemStack.getAmount() == 1) {
                            // if not allowed, deny
                            if (isNotAllowedInBeacon(cursorItemStack)) {
                                event.setResult(Event.Result.DENY);
                                return;
                            }
                        }
                    }
                    break;
                case PLAYER:
                    // if shift click, item could potentially be moved to beacon
                    if (event.isShiftClick()) {
                        ItemStack clicked = event.getCurrentItem();
                        // if no item in clicked slot, then nothing can be moved to beacon anyway
                        if (clicked == null) {
                            return;
                        }
                        // if air or more than 1 item, then nothing will be moved to beacon
                        if (clicked.getType() == Material.AIR || clicked.getAmount() > 1) {
                            return;
                        }
                        // make sure top inventory is a beacon inventory
                        if (event.getWhoClicked().getOpenInventory().getTopInventory().getType() == InventoryType.BEACON) {
                            // if not allowed, deny
                            if (isNotAllowedInBeacon(clicked)) {
                                event.setResult(Event.Result.DENY);
                                return;
                            }
                        }
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onBeaconInventoryDragEvent(InventoryDragEvent event) {
        if (!event.isCancelled()) {
            if (event.getInventory().getType() == InventoryType.BEACON) {
                Collection<ItemStack> values = event.getNewItems().values();
                for (ItemStack draggedResult : values) {
                    // if not allowed, deny
                    if (isNotAllowedInBeacon(draggedResult)) {
                        event.setResult(Event.Result.DENY);
                        return;
                    }
                }
            }
        }
    }

    private boolean isNotAllowedInBeacon(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case IRON_INGOT:
            case GOLD_INGOT:
            case DIAMOND:
            case EMERALD:
            case NETHERITE_INGOT:
                // check if plant item
                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                return plantItem != null && !plantItem.isAllowBeacon();
            default:
                return false;
        }
    }
    //endregion

    //region Wearable Item Cancelling
    @EventHandler
    public void onBlockDispenseArmorEvent(BlockDispenseArmorEvent event) {
        if (!event.isCancelled()) {
            // check if material is wearable
            if (ItemHelper.isMaterialWearable(event.getItem())) {
                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getItem());
                if (plantItem != null && !plantItem.isAllowWear()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onWearableInventoryDragEvent(InventoryDragEvent event) {
        if (!event.isCancelled()) {
            if (event.getInventory().getType() == InventoryType.CRAFTING) {
                for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
                    switch(entry.getKey()) {
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(entry.getValue());
                            if (plantItem != null && !plantItem.isAllowWear()) {
                                event.setCancelled(true);
                            }
                            return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWearableInventoryClickEvent(InventoryClickEvent event) {
        // completely block interaction with result
        if (!event.isCancelled()) {
            if (event.getClickedInventory() != null) {
                if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                    // check if shift click
                    if (event.getClick().isShiftClick()) {
                        // if shift clicked armor slot, then do nothing (is taking off armor)
                        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                            return;
                        }
                        Inventory inventory = event.getWhoClicked().getOpenInventory().getTopInventory();
                        // if top inventory is CRAFTING, then armor slots are available
                        if (inventory.getType() == InventoryType.CRAFTING) {
                            // if clicked item is not wearable, cancel click
                            if (ItemHelper.isMaterialWearable(event.getCurrentItem())) {
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getCurrentItem());
                                if (plantItem != null && !plantItem.isAllowWear()) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                    else {
                        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                            if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                                return;
                            }
                            if (ItemHelper.isMaterialWearable(event.getCursor())) {
                                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getCursor());
                                if (plantItem != null && !plantItem.isAllowWear()) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion

}
