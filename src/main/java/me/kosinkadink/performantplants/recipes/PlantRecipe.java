package me.kosinkadink.performantplants.recipes;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import org.bukkit.inventory.Recipe;

public class PlantRecipe {

    private Recipe recipe;
    private ScriptBlock interact;
    private boolean ignoreResult = false;

    public PlantRecipe(Recipe recipe, ScriptBlock interact) {
        this.recipe = recipe;
        this.interact = interact;
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

    public ScriptBlock getInteract() {
        return interact;
    }

    public void setInteract(ScriptBlock interact) {
        this.interact = interact;
    }

    public boolean isIgnoreResult() {
        return ignoreResult;
    }

    public void setIgnoreResult(boolean ignoreResult) {
        this.ignoreResult = ignoreResult;
    }
}
