package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.storage.PlantInteractStorage;
import org.bukkit.inventory.Recipe;

public class PlantRecipe {

    private Recipe recipe;
    private PlantInteractStorage storage;
    private boolean ignoreResult = false;

    public PlantRecipe(Recipe recipe, PlantInteractStorage storage) {
        this.recipe = recipe;
        this.storage = storage;
    }

    public PlantRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public PlantInteractStorage getStorage() {
        return storage;
    }

    public void setStorage(PlantInteractStorage storage) {
        this.storage = storage;
    }

    public boolean isIgnoreResult() {
        return ignoreResult;
    }

    public void setIgnoreResult(boolean ignoreResult) {
        this.ignoreResult = ignoreResult;
    }
}
