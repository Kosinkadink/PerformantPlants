package me.kosinkadink.performantplants.recipes.keys;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PotionRecipeKey extends RecipeKey {

    private final ItemStack ingredient;
    private final ItemStack potion;

    public PotionRecipeKey(ItemStack ingredient, ItemStack potion) {
        this.ingredient = ingredient.clone();
        this.potion = potion.clone();
        this.ingredient.setAmount(1);
        this.potion.setAmount(1);
    }

    public ItemStack getIngredient() {
        return ingredient;
    }

    public ItemStack getPotion() {
        return potion;
    }

    @Override
    public boolean equals(Object o) {
        // true if refers to this object
        if (this == o) {
            return true;
        }
        // false if object is null or not of same class
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PotionRecipeKey fromO = (PotionRecipeKey)o;
        // true if components match, false otherwise
        return ingredient.isSimilar(fromO.ingredient) && potion.isSimilar(fromO.potion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, potion);
    }
}
