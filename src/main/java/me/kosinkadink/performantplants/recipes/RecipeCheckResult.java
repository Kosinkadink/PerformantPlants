package me.kosinkadink.performantplants.recipes;

public class RecipeCheckResult {

    private final boolean allow;
    private final PlantRecipe recipe;

    public RecipeCheckResult(boolean allow, PlantRecipe recipe) {
        this.allow = allow;
        this.recipe = recipe;
    }

    public static RecipeCheckResult deny() {
        return new RecipeCheckResult(false, null);
    }

    public static RecipeCheckResult allow(PlantRecipe recipe) {
        return new RecipeCheckResult(true, recipe);
    }

    public boolean isAllow() {
        return allow;
    }

    public PlantRecipe getRecipe() {
        return recipe;
    }

}
