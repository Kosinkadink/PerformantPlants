package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class RecipeManager {

    private Main main;

    private HashMap<String, ShapedRecipe> shapedRecipeMap = new HashMap<>();
    private HashMap<String, ShapelessRecipe> shapelessRecipeMap = new HashMap<>();
    private HashMap<String, FurnaceRecipe> furnaceRecipeMap = new HashMap<>();

    public RecipeManager(Main main) {
        this.main = main;
    }

    public boolean isRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return shapedRecipeMap.get(((ShapedRecipe) recipe).getKey().getKey()) != null;
        } else if (recipe instanceof ShapelessRecipe) {
            return shapelessRecipeMap.get(((ShapelessRecipe) recipe).getKey().getKey()) != null;
        }
        return false;
    }

    public boolean isInputForFurnaceRecipe(ItemStack itemStack) {
        for (FurnaceRecipe recipe : furnaceRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public Recipe getRecipe(String key) {
        Recipe returned = shapedRecipeMap.get(key);
        if (returned == null) {
            returned = shapelessRecipeMap.get(key);
        }
        return returned;

    }

    public void addRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe convertedRecipe = (ShapedRecipe) recipe;
            shapedRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe convertedRecipe = (ShapelessRecipe) recipe;
            shapelessRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof FurnaceRecipe) {
            FurnaceRecipe convertedRecipe = (FurnaceRecipe) recipe;
            furnaceRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        }
    }

}
