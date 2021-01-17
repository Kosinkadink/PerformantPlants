package me.kosinkadink.performantplants.util;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.builders.ItemBuilder;
import me.kosinkadink.performantplants.plants.PlantItem;
import me.kosinkadink.performantplants.recipes.PlantRecipe;
import me.kosinkadink.performantplants.recipes.PlantSmithingRecipe;
import me.kosinkadink.performantplants.recipes.RecipeCheckResult;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class RecipeHelper {

    private static final ItemStack invalidResult;

    static {
        invalidResult = new ItemBuilder(Material.BARRIER)
                .displayName(TextHelper.translateAlternateColorCodes("&cInvalid Recipe"))
                .build();
    }

    public static ItemStack getInvalidResult() {
        return invalidResult.clone();
    }

    public static boolean isInvalidResult(ItemStack result) {
        return result.isSimilar(invalidResult);
    }

    public static RecipeCheckResult checkSmithingRecipe(SmithingInventory inventory) {
        // get base
        ItemStack baseItemStack = inventory.getItem(0);
        if (baseItemStack == null) {
            // return null, meaning it should behave as is
            return null;
        }
        PlantItem basePlantItem = PerformantPlants.getInstance().getPlantTypeManager().getPlantItemByItemStack(baseItemStack);
        // get addition
        ItemStack addItemStack = inventory.getItem(1);
        PlantItem addPlantItem = null;
        if (addItemStack != null) {
            addPlantItem = PerformantPlants.getInstance().getPlantTypeManager().getPlantItemByItemStack(addItemStack);
            // check if registered smithing recipe
            PlantRecipe recipe = PerformantPlants.getInstance().getRecipeManager().getSmithingRecipe(new SmithingRecipeKey(baseItemStack, addItemStack));
            if (recipe != null) {
                PlantSmithingRecipe smithingRecipe = (PlantSmithingRecipe) recipe.getRecipe();
                SmithingRecipeKey recipeKey = smithingRecipe.getRecipeKey();
                if (!recipeKey.getBase().isSimilar(baseItemStack)) {
                    return RecipeCheckResult.deny();
                }
                ItemStack recipeAddition = recipeKey.getAddition();
                if (recipeAddition != null && !recipeAddition.isSimilar(addItemStack)) {
                    return RecipeCheckResult.deny();
                }
                return RecipeCheckResult.allow(recipe);
            }
        }
        if (basePlantItem != null && !basePlantItem.isAllowSmithing()) {
            return RecipeCheckResult.deny();
        }
        if (addPlantItem != null && !addPlantItem.isAllowSmithing()) {
            return RecipeCheckResult.deny();
        }
        return null;
    }

    public static RecipeCheckResult checkAnvilRecipe(AnvilInventory inventory) {
        // null means to do vanilla behavior
        RecipeCheckResult recipeCheckResult = null;
        // get rename
        String renameText = inventory.getRenameText();
        // get first item
        ItemStack firstItemStack = inventory.getStorageContents()[0];
        // if first item not set, do nothing
        if (firstItemStack == null) {
            // return null, meaning it should behave as is
            return null;
        }
        PlantItem firstPlantItem = PerformantPlants.getInstance().getPlantTypeManager().getPlantItemByItemStack(firstItemStack);
        if (firstPlantItem != null) {
            if (!firstPlantItem.isAllowAnvilRename() && renameText != null && !renameText.isEmpty()) {
                // deny
                recipeCheckResult = RecipeCheckResult.deny();
            }
        }
        ItemStack secondItemStack = inventory.getStorageContents()[1];
        PlantItem secondPlantItem = null;
        if (secondItemStack != null) {
            secondPlantItem = PerformantPlants.getInstance().getPlantTypeManager().getPlantItemByItemStack(secondItemStack);
        }
        if (secondPlantItem != null) {
            if (firstPlantItem != null && !firstPlantItem.isAllowAnvil()) {
                // deny
                recipeCheckResult = RecipeCheckResult.deny();
            }
            if (!secondPlantItem.isAllowAnvil()) {
                // deny
                recipeCheckResult = RecipeCheckResult.deny();
            }
        }
        if (secondItemStack != null) {
            // check if anvil recipe is registered
            if (renameText != null && renameText.equals(ChatColor.stripColor(ItemHelper.getDisplayName(firstItemStack)))) {
                renameText = "";
            }
            AnvilRecipeKey recipeKey = new AnvilRecipeKey(firstItemStack, secondItemStack, renameText);
            PlantRecipe recipe = PerformantPlants.getInstance().getRecipeManager().getAnvilRecipe(recipeKey);
            if (recipe != null) {
                recipeCheckResult = RecipeCheckResult.allow(recipe);
            }
        }
        return recipeCheckResult;
    }

}
