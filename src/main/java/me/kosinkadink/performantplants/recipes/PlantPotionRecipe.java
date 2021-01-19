package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class PlantPotionRecipe extends PlantGeneralRecipe {

    private final PotionRecipeKey recipeKey;

    public PlantPotionRecipe(PotionRecipeKey recipeKey, ItemStack result, NamespacedKey namespacedKey) {
        this.recipeKey = recipeKey;
        this.result = result;
        this.namespacedKey = namespacedKey;
    }

    public PotionRecipeKey getRecipeKey() {
        return recipeKey;
    }

}
