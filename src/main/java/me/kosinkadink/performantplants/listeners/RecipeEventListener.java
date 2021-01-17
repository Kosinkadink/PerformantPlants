package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.PlantAnvilRecipe;
import me.kosinkadink.performantplants.recipes.PlantRecipe;
import me.kosinkadink.performantplants.recipes.PlantSmithingRecipe;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

public class RecipeEventListener implements Listener {

    private final PerformantPlants performantPlants;

    public RecipeEventListener(PerformantPlants performantPlantsClass) {
        performantPlants = performantPlantsClass;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        // check if a vanilla recipe is trying to use a plant item
        if (!performantPlants.getRecipeManager().isRecipe(event.getRecipe())) {
            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                // if plant item is used and vanilla crafting not allowed, set result to air
                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(ingredient);
                if (plantItem != null && !plantItem.isAllowCrafting()) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!event.isCancelled()) {
            PlantRecipe plantRecipe = performantPlants.getRecipeManager().getRecipe(event.getRecipe());
            if (plantRecipe == null) {
                return;
            }
            PlantInteractStorage storage = plantRecipe.getStorage();
            if (storage != null) {
                PlantInteract interact = storage.getPlantInteract(event.getWhoClicked().getInventory().getItemInMainHand(), (Player) event.getWhoClicked(), null);
                if (interact != null) {
                    if (plantRecipe.isIgnoreResult()) {
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        // removing current item causes ingredients not to be consumed
                        // so consume them manually
                        for (ItemStack itemStack : event.getInventory().getMatrix()) {
                            if (itemStack != null && itemStack.getType() != Material.AIR) {
                                itemStack.setAmount(itemStack.getAmount()-1);
                            }
                        }
                    }
                    // perform effects on block
                    Block block = null;
                    try {
                        block = event.getInventory().getLocation().getBlock();
                        interact.getEffectStorage().performEffects(block, PlantBlock.wrapBlock(block));
                    } catch (NullPointerException ignored) { }
                    // perform effects on player, if any
                    PlantConsumableStorage consumableStorage = interact.getConsumableStorage();
                    if (consumableStorage != null) {
                        Player player = (Player) event.getWhoClicked();
                        PlantConsumable consumable = consumableStorage.getConsumable(player, EquipmentSlot.HAND);
                        if (consumable != null) {
                            consumable.getEffectStorage().performEffects(player, PlantBlock.wrapBlock(block));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        // check if a smelting recipe was registered for item stack
        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getSource());
        if (plantItem != null) {
            switch (event.getBlock().getType()) {
                case FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForFurnaceRecipe(event.getSource())) {
                        event.setCancelled(true);
                    } break;
                case BLAST_FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForBlastingRecipe(event.getSource())) {
                        event.setCancelled(true);
                    } break;
                case SMOKER:
                    if (!performantPlants.getRecipeManager().isInputForSmokingRecipe(event.getSource())) {
                        event.setCancelled(true);
                    } break;
                case CAMPFIRE:
                    if (!performantPlants.getRecipeManager().isInputForCampfireRecipe(event.getSource())) {
                        event.setCancelled(true);
                    } break;
            }
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        // check if a plant item is about to be used as fuel
        // cancel if plant
        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getFuel());
        if (plantItem != null) {
            // check if plant item has burn time
            if (plantItem.hasBurnTime()) {
                int burnTime = plantItem.getBurnTime();
                Material furnaceType = event.getBlock().getType();
                // half the burn time for Blast Furnace and Smoker
                if (furnaceType == Material.BLAST_FURNACE || furnaceType == Material.SMOKER) {
                    event.setBurnTime(Math.max(1,burnTime/2));
                } else {
                    event.setBurnTime(burnTime);
                }
            }
            // if not, set burn time to zero
            else if (!plantItem.isAllowFuel()){
                Furnace furnace = (Furnace)event.getBlock().getState();
                short time = furnace.getCookTime();
                if (time > 0) {
                    furnace.setCookTime((short) Math.max(0, time - 2));
                    furnace.setBurnTime((short) 0);
                    furnace.update();
                }
                event.setCancelled(true);
                return;
            }
        }
        // check if a plant item is trying to be cooked
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        FurnaceInventory furnaceInventory = (FurnaceInventory) inventoryHolder.getInventory();
        // check if a corresponding smelting recipe was registered for item stack
        plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(furnaceInventory.getSmelting());
        if (plantItem != null) {
            switch (event.getBlock().getType()) {
                case FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForFurnaceRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                    }
                    break;
                case SMOKER:
                    if (!performantPlants.getRecipeManager().isInputForSmokingRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                    }
                    break;
                case BLAST_FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForBlastingRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onPrepareSmithingEvent(PrepareSmithingEvent event) {
        SmithingInventory inventory = event.getInventory();
        // get base
        ItemStack baseItemStack = inventory.getItem(0);
        if (baseItemStack == null) {
            return;
        }
        PlantItem basePlantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(baseItemStack);
        // get addition
        ItemStack addItemStack = inventory.getItem(1);
        PlantItem addPlantItem = null;
        if (addItemStack != null) {
            addPlantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(addItemStack);
            // check if registered smithing recipe
            PlantRecipe recipe = performantPlants.getRecipeManager().getSmithingRecipe(new SmithingRecipeKey(baseItemStack, addItemStack));
            if (recipe != null) {
                PlantSmithingRecipe smithingRecipe = (PlantSmithingRecipe) recipe.getRecipe();
                SmithingRecipeKey recipeKey = smithingRecipe.getRecipeKey();
                if (!recipeKey.getBase().isSimilar(baseItemStack)) {
                    event.setResult(new ItemStack(Material.AIR));
                    return;
                }
                ItemStack recipeAddition = recipeKey.getAddition();
                if (recipeAddition != null && !recipeAddition.isSimilar(addItemStack)) {
                    event.setResult(new ItemStack(Material.AIR));
                    return;
                }
                event.setResult(smithingRecipe.getResult());
                // TODO: perform actions
                return;
            }
        }
        if (basePlantItem != null && !basePlantItem.isAllowSmithing()) {
            event.setResult(new ItemStack(Material.AIR));
            return;
        }
        if (addPlantItem != null && !addPlantItem.isAllowSmithing()) {
            event.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onPrepareAnvilEvent(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        // get rename
        String renameText = inventory.getRenameText();
        // get first item
        ItemStack firstItemStack = inventory.getStorageContents()[0];
        // if first item not set, do nothing
        if (firstItemStack == null) {
            return;
        }
        PlantItem firstPlantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(firstItemStack);
        if (firstPlantItem != null) {
            if (!firstPlantItem.isAllowAnvilRename() && renameText != null && !renameText.isEmpty()) {
                event.setResult(new ItemStack(Material.AIR));
            }
        }
        ItemStack secondItemStack = inventory.getStorageContents()[1];
        PlantItem secondPlantItem = null;
        if (secondItemStack != null) {
            secondPlantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(secondItemStack);
        }
        if (secondPlantItem != null) {
            if (firstPlantItem != null && !firstPlantItem.isAllowAnvil()) {
                event.setResult(new ItemStack(Material.AIR));
            }
            if (!secondPlantItem.isAllowAnvil()) {
                event.setResult(new ItemStack(Material.AIR));
            }
        }
        if (secondItemStack != null) {
            // check if anvil recipe is registered
            AnvilRecipeKey recipeKey = new AnvilRecipeKey(firstItemStack, secondItemStack, renameText);
            PlantRecipe recipe = performantPlants.getRecipeManager().getAnvilRecipe(recipeKey);
            if (recipe != null) {
                PlantAnvilRecipe anvilRecipe = (PlantAnvilRecipe) recipe.getRecipe();
                event.setResult(anvilRecipe.getResult());
                inventory.setRepairCost(anvilRecipe.getLevelCost());
                // TODO: perform actions
            }
        }
    }

    @EventHandler
    public void onPrepareItemEnchantEvent(PrepareItemEnchantEvent event) {
        if (!event.isCancelled()) {
            Inventory inventory = event.getInventory();
            for (ItemStack itemStack : inventory.getStorageContents()) {
                PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(itemStack);
                if (plantItem != null && !plantItem.isAllowEnchanting()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
