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
        // TODO: remove when done testing
//        addShapedRecipe();
//        addShapelessRecipe();
//        addFurnaceRecipe();
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

//     TODO: remove this after testing
//    public void addShapedRecipe() {
//        ItemStack result = main.getPlantTypeManager().getPlantItemStackById("weed");
//        result.setAmount(3);
//        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(main,"test_recipe1"), result);
//        recipe.shape("XX", "XX");
//        ItemStack ingredient1 = main.getPlantTypeManager().getPlantItemStackById("test");
//        recipe.setIngredient('X', new RecipeChoice.ExactChoice(ingredient1));
//        main.getServer().addRecipe(recipe);
//        addRecipe(recipe);
//        main.getLogger().info("Added shaped recipe");
//    }
//
//    public void addShapelessRecipe() {
//        ItemStack result = main.getPlantTypeManager().getPlantItemStackById("cocaine");
//        result.setAmount(4);
//        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(main, "test_shapeless1"), result);
//        ItemStack ingredient1 = main.getPlantTypeManager().getPlantItemStackById("cocaine.seed");
//        recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient1));
//        recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient1));
//        main.getServer().addRecipe(recipe);
//        addRecipe(recipe);
//        main.getLogger().info("Added shapeless recipe");
//    }
//
//    public void addFurnaceRecipe() {
//        ItemStack input = main.getPlantTypeManager().getPlantItemStackById("test.seed");
//        ItemStack result = main.getPlantTypeManager().getPlantItemStackById("test");
//        FurnaceRecipe recipe = new FurnaceRecipe(new NamespacedKey(main, "furnace_test1"),
//                result, new RecipeChoice.ExactChoice(input),
//                1,
//                20);
//        main.getServer().addRecipe(recipe);
//        addRecipe(recipe);
//        main.getLogger().info("Added furnace recipe");
//    }

}
