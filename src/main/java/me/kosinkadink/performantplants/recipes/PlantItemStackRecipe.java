package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.recipes.keys.ItemStackRecipeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class PlantItemStackRecipe extends PlantGeneralRecipe {

    private final ItemStackRecipeKey recipeKey;

    public PlantItemStackRecipe(ItemStackRecipeKey recipeKey, ItemStack result, NamespacedKey namespacedKey) {
        this.recipeKey = recipeKey;
        this.result = result;
        this.namespacedKey = namespacedKey;
    }

    public ItemStackRecipeKey getRecipeKey() {
        return recipeKey;
    }

}
