package me.kosinkadink.performantplants.managers;

import me.kosinkadink.performantplants.Main;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class RecipeManager {

    private Main main;

    private HashMap<String, ShapedRecipe> shapedRecipeMap = new HashMap<>();
    private HashMap<String, ShapelessRecipe> shapelessRecipeMap = new HashMap<>();
    private HashMap<String, FurnaceRecipe> furnaceRecipeMap = new HashMap<>();
    private HashMap<String, BlastingRecipe> blastingRecipeMap = new HashMap<>();
    private HashMap<String, SmokingRecipe> smokingRecipeMap = new HashMap<>();
    private HashMap<String, CampfireRecipe> campfireRecipeMap = new HashMap<>();
    private HashMap<String, StonecuttingRecipe> stonecuttingRecipeMap = new HashMap<>();

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

}
