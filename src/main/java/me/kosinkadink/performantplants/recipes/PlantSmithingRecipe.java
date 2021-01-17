package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class PlantSmithingRecipe extends PlantGeneralRecipe {

    private final SmithingRecipeKey recipeKey;

    public PlantSmithingRecipe(SmithingRecipeKey recipeKey, ItemStack result, NamespacedKey namespacedKey) {
        this.recipeKey = recipeKey;
        this.result = result;
        this.namespacedKey = namespacedKey;
    }

    public SmithingRecipeKey getRecipeKey() {
        return recipeKey;
    }

}
