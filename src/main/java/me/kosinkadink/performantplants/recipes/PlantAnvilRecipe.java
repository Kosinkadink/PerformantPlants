package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class PlantAnvilRecipe extends PlantGeneralRecipe {

    private final AnvilRecipeKey recipeKey;
    private final int levelCost;

    public PlantAnvilRecipe(AnvilRecipeKey recipeKey, ItemStack result, int levelCost, NamespacedKey namespacedKey) {
        this.recipeKey = recipeKey;
        this.result = result;
        this.levelCost = levelCost;
        this.namespacedKey = namespacedKey;
    }

    public AnvilRecipeKey getRecipeKey() {
        return recipeKey;
    }

    public int getLevelCost() {
        return levelCost;
    }

}
