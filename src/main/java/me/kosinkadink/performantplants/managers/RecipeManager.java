package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.recipes.*;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.ItemStackRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class RecipeManager {

    private final PerformantPlants performantPlants;

    private final HashMap<String, PlantRecipe> shapedRecipeMap = new HashMap<>();
    private final HashMap<String, PlantRecipe> shapelessRecipeMap = new HashMap<>();
    private final HashMap<ItemStackRecipeKey, PlantItemStackRecipe> furnaceRecipeMap = new HashMap<>();
    private final HashMap<ItemStackRecipeKey, PlantItemStackRecipe> blastingRecipeMap = new HashMap<>();
    private final HashMap<ItemStackRecipeKey, PlantItemStackRecipe> smokingRecipeMap = new HashMap<>();
    private final HashMap<ItemStackRecipeKey, PlantItemStackRecipe> campfireRecipeMap = new HashMap<>();
    private final HashMap<String, StonecuttingRecipe> stonecuttingRecipeMap = new HashMap<>();
    private final HashMap<SmithingRecipeKey, PlantRecipe> smithingRecipeMap = new HashMap<>();
    private final HashMap<AnvilRecipeKey, PlantRecipe> anvilRecipeMap = new HashMap<>();
    private final HashMap<PotionRecipeKey, PlantPotionRecipe> potionRecipeMap = new HashMap<>();

    public RecipeManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
    }

    public boolean isCraftingRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return shapedRecipeMap.get(((ShapedRecipe) recipe).getKey().getKey()) != null;
        } else if (recipe instanceof ShapelessRecipe) {
            return shapelessRecipeMap.get(((ShapelessRecipe) recipe).getKey().getKey()) != null;
        }
        return false;
    }

    public boolean isInputForFurnaceRecipe(ItemStack itemStack) {
        ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(itemStack);
        return furnaceRecipeMap.get(recipeKey) != null;
    }

    public boolean isInputForBlastingRecipe(ItemStack itemStack) {
        ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(itemStack);
        return blastingRecipeMap.get(recipeKey) != null;
    }

    public boolean isInputForSmokingRecipe(ItemStack itemStack) {
        ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(itemStack);
        return smokingRecipeMap.get(recipeKey) != null;
    }

    public boolean isInputForCampfireRecipe(ItemStack itemStack) {
        ItemStackRecipeKey recipeKey = new ItemStackRecipeKey(itemStack);
        return campfireRecipeMap.get(recipeKey) != null;
    }

    public boolean isInputForStonecuttingRecipe(ItemStack itemStack) {
        for (StonecuttingRecipe recipe : stonecuttingRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public PlantRecipe getRecipe(Recipe recipe) {
        PlantRecipe returned = null;
        if (recipe instanceof ShapedRecipe) {
            returned = shapedRecipeMap.get(((ShapedRecipe) recipe).getKey().getKey());
        } else if (recipe instanceof ShapelessRecipe) {
            returned = shapelessRecipeMap.get(((ShapelessRecipe) recipe).getKey().getKey());
        }
        return returned;
    }

    public PlantRecipe getSmithingRecipe(SmithingRecipeKey recipeKey) {
        return smithingRecipeMap.get(recipeKey);
    }

    public PlantRecipe getAnvilRecipe(AnvilRecipeKey recipeKey) {
        return anvilRecipeMap.get(recipeKey);
    }

    public PlantPotionRecipe getPotionRecipe(PotionRecipeKey recipeKey) {
        return potionRecipeMap.get(recipeKey);
    }

    public void addShapedRecipe(PlantRecipe recipe) {
        shapedRecipeMap.put(((ShapedRecipe) recipe.getRecipe()).getKey().getKey(), recipe);
    }

    public void addShapelessRecipe(PlantRecipe recipe) {
        shapelessRecipeMap.put(((ShapelessRecipe) recipe.getRecipe()).getKey().getKey(), recipe);
    }

    public void addFurnaceRecipe(PlantItemStackRecipe recipe) {
        furnaceRecipeMap.put(recipe.getRecipeKey(), recipe);
    }

    public void addBlastingRecipe(PlantItemStackRecipe recipe) {
        blastingRecipeMap.put(recipe.getRecipeKey(), recipe);
    }

    public void addSmokingRecipe(PlantItemStackRecipe recipe) {
        smokingRecipeMap.put(recipe.getRecipeKey(), recipe);
    }

    public void addCampfireRecipe(PlantItemStackRecipe recipe) {
        campfireRecipeMap.put(recipe.getRecipeKey(), recipe);
    }

    public void addStonecuttingRecipe(StonecuttingRecipe recipe) {
        stonecuttingRecipeMap.put(recipe.getKey().getKey(), recipe);
    }

    public void addSmithingRecipe(PlantRecipe recipe) {
        smithingRecipeMap.put(((PlantSmithingRecipe)recipe.getRecipe()).getRecipeKey(), recipe);
    }

    public void addAnvilRecipe(PlantRecipe recipe) {
        anvilRecipeMap.put(((PlantAnvilRecipe)recipe.getRecipe()).getRecipeKey(), recipe);
    }

    public void addPotionRecipe(PlantPotionRecipe recipe) {
        potionRecipeMap.put(recipe.getRecipeKey(), recipe);
    }

    public void clearRecipes() {
        clearShapedRecipes();
        clearShapelessRecipes();
        clearFurnaceRecipes();
        clearBlastingRecipes();
        clearSmokingRecipes();
        clearCampfireRecipes();
        clearStonecuttingRecipes();
        clearSmithingRecipes();
        clearAnvilRecipes();
        clearPotionRecipes();
    }

    private void clearShapedRecipes() {
        for (PlantRecipe plantRecipe : shapedRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(((ShapedRecipe) plantRecipe.getRecipe()).getKey());
        }
        shapedRecipeMap.clear();
    }

    private void clearShapelessRecipes() {
        for (PlantRecipe plantRecipe : shapelessRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(((ShapelessRecipe) plantRecipe.getRecipe()).getKey());
        }
        shapelessRecipeMap.clear();
    }

    private void clearFurnaceRecipes() {
        for (PlantItemStackRecipe recipe : furnaceRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        furnaceRecipeMap.clear();
    }

    private void clearBlastingRecipes() {
        for (PlantItemStackRecipe recipe : blastingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        blastingRecipeMap.clear();
    }

    private void clearSmokingRecipes() {
        for (PlantItemStackRecipe recipe : smokingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        smokingRecipeMap.clear();
    }

    private void clearCampfireRecipes() {
        for (PlantItemStackRecipe recipe : campfireRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        campfireRecipeMap.clear();
    }

    private void clearStonecuttingRecipes() {
        for (StonecuttingRecipe recipe : stonecuttingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        stonecuttingRecipeMap.clear();
    }

    private void clearSmithingRecipes() {
        for (PlantRecipe recipe : smithingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(((PlantSmithingRecipe)recipe.getRecipe()).getKey());
        }
        smithingRecipeMap.clear();
    }

    private void clearAnvilRecipes() {
        smithingRecipeMap.clear();
    }

    private void clearPotionRecipes() {
        potionRecipeMap.clear();
    }

}
