package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.recipes.PlantAnvilRecipe;
import me.kosinkadink.performantplants.recipes.PlantPotionRecipe;
import me.kosinkadink.performantplants.recipes.PlantRecipe;
import me.kosinkadink.performantplants.recipes.PlantSmithingRecipe;
import me.kosinkadink.performantplants.recipes.keys.AnvilRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.PotionRecipeKey;
import me.kosinkadink.performantplants.recipes.keys.SmithingRecipeKey;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class RecipeManager {

    private final PerformantPlants performantPlants;

    private final HashMap<String, PlantRecipe> shapedRecipeMap = new HashMap<>();
    private final HashMap<String, PlantRecipe> shapelessRecipeMap = new HashMap<>();
    private final HashMap<String, FurnaceRecipe> furnaceRecipeMap = new HashMap<>();
    private final HashMap<String, BlastingRecipe> blastingRecipeMap = new HashMap<>();
    private final HashMap<String, SmokingRecipe> smokingRecipeMap = new HashMap<>();
    private final HashMap<String, CampfireRecipe> campfireRecipeMap = new HashMap<>();
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
        for (FurnaceRecipe recipe : furnaceRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInputForBlastingRecipe(ItemStack itemStack) {
        for (BlastingRecipe recipe : blastingRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInputForSmokingRecipe(ItemStack itemStack) {
        for (SmokingRecipe recipe : smokingRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInputForCampfireRecipe(ItemStack itemStack) {
        for (CampfireRecipe recipe : campfireRecipeMap.values()) {
            if (recipe.getInput().isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
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

    public void addFurnaceRecipe(FurnaceRecipe recipe) {
        furnaceRecipeMap.put(recipe.getKey().getKey(), recipe);
    }

    public void addBlastingRecipe(BlastingRecipe recipe) {
        blastingRecipeMap.put(recipe.getKey().getKey(), recipe);
    }

    public void addSmokingRecipe(SmokingRecipe recipe) {
        smokingRecipeMap.put(recipe.getKey().getKey(), recipe);
    }

    public void addCampfireRecipe(CampfireRecipe recipe) {
        campfireRecipeMap.put(recipe.getKey().getKey(), recipe);
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
        for (FurnaceRecipe recipe : furnaceRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        furnaceRecipeMap.clear();
    }

    private void clearBlastingRecipes() {
        for (BlastingRecipe recipe : blastingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        blastingRecipeMap.clear();
    }

    private void clearSmokingRecipes() {
        for (SmokingRecipe recipe : smokingRecipeMap.values()) {
            performantPlants.getServer().removeRecipe(recipe.getKey());
        }
        smokingRecipeMap.clear();
    }

    private void clearCampfireRecipes() {
        for (CampfireRecipe recipe : campfireRecipeMap.values()) {
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
