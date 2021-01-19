package me.kosinkadink.performantplants.settings;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CookingRecipeSettings {

    private ItemStack result;
    private ItemStack input;
    private float experience;
    private int cookingTime;

    public CookingRecipeSettings(ItemStack result, ItemStack input, float experience, int cookingTime) {
        this.result = result;
        this.input = input;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getInput() {
        return input;
    }

    public void setInput(ItemStack input) {
        this.input = input;
    }

    public RecipeChoice getInputChoice() {
        return new RecipeChoice.ExactChoice(input);
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
