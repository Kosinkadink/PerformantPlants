package me.kosinkadink.performantplants.settings;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CookingRecipeSettings {

    private ItemStack result;
    private RecipeChoice inputChoice;
    private float experience;
    private int cookingTime;

    public CookingRecipeSettings(ItemStack result, RecipeChoice inputChoice, float experience, int cookingTime) {
        this.result = result;
        this.inputChoice = inputChoice;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public RecipeChoice getInputChoice() {
        return inputChoice;
    }

    public void setInputChoice(RecipeChoice inputChoice) {
        this.inputChoice = inputChoice;
    }

    public float getExperience() {
        return experience;
    }

    public void setExperience(float experience) {
        this.experience = experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }
}
