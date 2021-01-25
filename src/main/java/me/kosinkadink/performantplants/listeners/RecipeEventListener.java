package me.kosinkadink.performantplants.listeners;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.plants.PlantConsumable;
import me.kosinkadink.performantplants.plants.PlantInteract;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.*;
import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import me.kosinkadink.performantplants.util.RecipeHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
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

    //region Crafting Table Stuff
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        // check if a vanilla recipe is trying to use a plant item
        if (!performantPlants.getRecipeManager().isCraftingRecipe(event.getRecipe())) {
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
    //endregion

    //region Furnace Stuff
    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        // check if a smelting recipe was registered for item stack
        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getSource());
        if (plantItem != null && !plantItem.isAllowSmelting()) {
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
                case SOUL_CAMPFIRE:
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
            // if not, simulate no burn for furnace
            else if (!plantItem.isAllowFuel()){
                Furnace furnace = (Furnace)event.getBlock().getState();
                simulateNoBurnForFurnace(furnace);
                event.setCancelled(true);
                return;
            }
        }
        // check if a plant item is trying to be cooked
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        FurnaceInventory furnaceInventory = (FurnaceInventory) inventoryHolder.getInventory();
        // check if a corresponding smelting recipe was registered for item stack
        plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(furnaceInventory.getSmelting());
        if (plantItem != null && !plantItem.isAllowSmelting()) {
            switch (event.getBlock().getType()) {
                case FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForFurnaceRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                        Furnace furnace = (Furnace)event.getBlock().getState();
                        simulateNoBurnForFurnace(furnace);
                    }
                    break;
                case SMOKER:
                    if (!performantPlants.getRecipeManager().isInputForSmokingRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                        Furnace furnace = (Furnace)event.getBlock().getState();
                        simulateNoBurnForFurnace(furnace);
                    }
                    break;
                case BLAST_FURNACE:
                    if (!performantPlants.getRecipeManager().isInputForBlastingRecipe(furnaceInventory.getSmelting())) {
                        event.setCancelled(true);
                        Furnace furnace = (Furnace)event.getBlock().getState();
                        simulateNoBurnForFurnace(furnace);
                    }
                    break;
            }
        }
    }

    private void simulateNoBurnForFurnace(Furnace furnace) {
        short time = furnace.getCookTime();
        if (time > 0) {
            // decrement cook time and set burn time to zero
            furnace.setCookTime((short) Math.max(0, time - 2));
            furnace.setBurnTime((short) 0);
            furnace.update();
        }
    }
    //endregion

    //region Brewing Stand Stuff
    @EventHandler
    public void onBrewEvent(BrewEvent event) {
        if (!event.isCancelled()) {
            BrewerInventory inventory = event.getContents();
            ItemStack ingredient = inventory.getIngredient();
            ItemStack[] vanillaCurrent = new ItemStack[3];
            ItemStack[] plantExpected = new ItemStack[3];
            if (ingredient != null) {
                // check if ingredient is a plant item
                PlantItem plantIngredient = performantPlants.getPlantTypeManager().getPlantItemByItemStack(ingredient);
                if (plantIngredient != null && !plantIngredient.isAllowIngredient()) {
                    event.setCancelled(true);
                    // replenish fuel
                    BrewingStand brewingStand = inventory.getHolder();
                    // add used fuel back
                    if (brewingStand != null) {
                        int newFuelLevel = brewingStand.getFuelLevel() + 1;
                        brewingStand.setFuelLevel(newFuelLevel);
                        brewingStand.update();
                    }
                    return;
                }
                int totalPlantPotionsBrewed = 0;
                int totalPotentialInvalidPlantPotionsBrewed = 0;
                for (int i = 0; i < 3; i++) {
                    ItemStack potion = inventory.getItem(i);
                    // if not null, check if recipe exists
                    if (potion != null) {
                        PotionRecipeKey recipeKey = new PotionRecipeKey(ingredient, potion);
                        PlantPotionRecipe potionRecipe = performantPlants.getRecipeManager().getPotionRecipe(recipeKey);
                        if (potionRecipe != null) {
                            plantExpected[i] = potionRecipe.getResult();
                            totalPlantPotionsBrewed++;
                            continue;
                        }
                        // check if potion is a plant item
                        PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(potion);
                        // if not allow brewing, make sure item will stay the same
                        if (plantItem != null && !plantItem.isAllowBrewing()) {
                            plantExpected[i] = potion;
                            totalPotentialInvalidPlantPotionsBrewed++;
                            continue;
                        }
                        // add as vanilla item to be potentially evaluated for changes
                        vanillaCurrent[i] = potion;
                    }
                }

                // potentially cleanup or cancel if nothing was supposed to be brewed
                if (totalPlantPotionsBrewed > 0 || totalPotentialInvalidPlantPotionsBrewed > 0) {
                    // if no plant potions were brewed, check if vanilla potions were brewed
                    // if not, do not use ingredient and replenish fuel usage
                    ItemStack ingredientClone = ingredient.clone();
                    boolean plantRecipesBrewed = totalPlantPotionsBrewed > 0;
                    performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                            RecipeHelper.cleanupBrewEventIfNoChange(inventory, vanillaCurrent, plantExpected,
                                    ingredientClone, plantRecipesBrewed)
                    );
                }
            }
        }
    }

    @EventHandler
    public void onBrewingStandFuelEvent(BrewingStandFuelEvent event) {
        if (!event.isCancelled()) {
            PlantItem plantItem = performantPlants.getPlantTypeManager().getPlantItemByItemStack(event.getFuel());
            if (plantItem != null) {
                // check if plant item has burn time
                if (plantItem.hasBurnTime()) {
                    int burnTime = plantItem.getBurnTime();
                    event.setFuelPower(burnTime);
                }
                // if not, don't use it if not allowed to act like fuel
                else if (!plantItem.isAllowFuel()){
                    event.setConsuming(false);
                    event.setFuelPower(0);
                    return;
                }
            }
            // TODO: check if plant item is trying to be brewed
        }
    }
    //endregion

    @EventHandler
    public void onPrepareSmithingEvent(PrepareSmithingEvent event) {
        SmithingInventory inventory = event.getInventory();
        // check recipe result
        RecipeCheckResult checkResult = RecipeHelper.checkSmithingRecipe(inventory);
        // if not null, process more (otherwise do vanilla behavior)
        if (checkResult != null) {
            // if allow, set result to recipe
            if (checkResult.isAllow()) {
                PlantRecipe recipe = checkResult.getRecipe();
                if (recipe != null) {
                    PlantSmithingRecipe smithingRecipe = (PlantSmithingRecipe) recipe.getRecipe();
                    event.setResult(smithingRecipe.getResult());
                }
            }
            // if deny, set result to invalid result
            else {
                event.setResult(RecipeHelper.getInvalidResult());
            }
        }
    }

    @EventHandler
    public void onPrepareAnvilEvent(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        // check recipe result
        RecipeCheckResult checkResult = RecipeHelper.checkAnvilRecipe(inventory);
        // if not null, process more (otherwise do vanilla behavior)
        if (checkResult != null) {
            // if allow, set result to recipe
            if (checkResult.isAllow()) {
                PlantRecipe recipe = checkResult.getRecipe();
                if (recipe != null) {
                    PlantAnvilRecipe anvilRecipe = (PlantAnvilRecipe) recipe.getRecipe();
                    event.setResult(anvilRecipe.getResult());
                    performantPlants.getServer().getScheduler().runTask(performantPlants, () ->
                            inventory.setRepairCost(anvilRecipe.getLevelCost())
                    );
                    // TODO: perform actions
                }
            }
            // if deny, set result to Air
            else {
                event.setResult(new ItemStack(Material.AIR));
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
