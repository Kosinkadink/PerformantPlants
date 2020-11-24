package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.plants.PlantRecipe;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class RecipeManager {

    private PerformantPlants performantPlants;

    private HashMap<String, PlantRecipe> shapedRecipeMap = new HashMap<>();
    private HashMap<String, PlantRecipe> shapelessRecipeMap = new HashMap<>();
    private HashMap<String, FurnaceRecipe> furnaceRecipeMap = new HashMap<>();
    private HashMap<String, BlastingRecipe> blastingRecipeMap = new HashMap<>();
    private HashMap<String, SmokingRecipe> smokingRecipeMap = new HashMap<>();
    private HashMap<String, CampfireRecipe> campfireRecipeMap = new HashMap<>();
    private HashMap<String, StonecuttingRecipe> stonecuttingRecipeMap = new HashMap<>();

    public RecipeManager(PerformantPlants performantPlants) {
        this.performantPlants = performantPlants;
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

    public void addRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe convertedRecipe = (ShapedRecipe) recipe;
            shapedRecipeMap.put(convertedRecipe.getKey().getKey(), new PlantRecipe(convertedRecipe));
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe convertedRecipe = (ShapelessRecipe) recipe;
            shapelessRecipeMap.put(convertedRecipe.getKey().getKey(), new PlantRecipe(convertedRecipe));
        } else if (recipe instanceof FurnaceRecipe) {
            FurnaceRecipe convertedRecipe = (FurnaceRecipe) recipe;
            furnaceRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof BlastingRecipe) {
            BlastingRecipe convertedRecipe = (BlastingRecipe) recipe;
            blastingRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof SmokingRecipe) {
            SmokingRecipe convertedRecipe = (SmokingRecipe) recipe;
            smokingRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof CampfireRecipe) {
            CampfireRecipe convertedRecipe = (CampfireRecipe) recipe;
            campfireRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        } else if (recipe instanceof StonecuttingRecipe) {
            StonecuttingRecipe convertedRecipe = (StonecuttingRecipe) recipe;
            stonecuttingRecipeMap.put(convertedRecipe.getKey().getKey(), convertedRecipe);
        }
    }

    public void addRecipe(PlantRecipe plantRecipe) {
        if (plantRecipe.getRecipe() == null) {
            return;
        }
        Recipe recipe = plantRecipe.getRecipe();
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe convertedRecipe = (ShapedRecipe) recipe;
            shapedRecipeMap.put(convertedRecipe.getKey().getKey(), plantRecipe);
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe convertedRecipe = (ShapelessRecipe) recipe;
            shapelessRecipeMap.put(convertedRecipe.getKey().getKey(), plantRecipe);
        }
    }

    public void clearRecipes() {
        clearShapedRecipes();
        clearShapelessRecipes();
        clearFurnaceRecipes();
        clearBlastingRecipes();
        clearSmokingRecipes();
        clearCampfireRecipes();
        clearStonecuttingRecipes();
    }

    private void clearRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe convertedRecipe = (ShapedRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe convertedRecipe = (ShapelessRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof FurnaceRecipe) {
            FurnaceRecipe convertedRecipe = (FurnaceRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof BlastingRecipe) {
            BlastingRecipe convertedRecipe = (BlastingRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof SmokingRecipe) {
            SmokingRecipe convertedRecipe = (SmokingRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof CampfireRecipe) {
            CampfireRecipe convertedRecipe = (CampfireRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        } else if (recipe instanceof StonecuttingRecipe) {
            StonecuttingRecipe convertedRecipe = (StonecuttingRecipe) recipe;
            performantPlants.getServer().removeRecipe(convertedRecipe.getKey());
        }
    }

    private void clearPlantRecipes(HashMap<String, PlantRecipe> recipeMap) {
        for (PlantRecipe recipe : recipeMap.values()) {
            clearRecipe(recipe.getRecipe());
        }
        recipeMap.clear();
    }

    private void clearShapedRecipes() {
        clearPlantRecipes(shapedRecipeMap);
    }

    private void clearShapelessRecipes() {
        clearPlantRecipes(shapelessRecipeMap);
    }

    private void clearFurnaceRecipes() {
        for (Recipe recipe : furnaceRecipeMap.values()) {
            clearRecipe(recipe);
        }
        furnaceRecipeMap.clear();
    }

    private void clearBlastingRecipes() {
        for (Recipe recipe : blastingRecipeMap.values()) {
            clearRecipe(recipe);
        }
        blastingRecipeMap.clear();
    }

    private void clearSmokingRecipes() {
        for (Recipe recipe : smokingRecipeMap.values()) {
            clearRecipe(recipe);
        }
        blastingRecipeMap.clear();
    }

    private void clearCampfireRecipes() {
        for (Recipe recipe : campfireRecipeMap.values()) {
            clearRecipe(recipe);
        }
        campfireRecipeMap.clear();
    }

    private void clearStonecuttingRecipes() {
        for (Recipe recipe : stonecuttingRecipeMap.values()) {
            clearRecipe(recipe);
        }
        stonecuttingRecipeMap.clear();
    }

}
