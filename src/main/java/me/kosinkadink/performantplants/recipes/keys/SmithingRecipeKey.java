package me.kosinkadink.performantplants.recipes.keys;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SmithingRecipeKey extends RecipeKey {

    private final ItemStack base;
    private final ItemStack addition;

    public SmithingRecipeKey(ItemStack base, ItemStack addition) {
        this.base = base.clone();
        this.addition = addition.clone();
        this.base.setAmount(1);
        this.addition.setAmount(1);
    }

    public ItemStack getBase() {
        return base;
    }

    public ItemStack getAddition() {
        return addition;
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
        SmithingRecipeKey fromO = (SmithingRecipeKey)o;
        // true if components match, false otherwise
        return base.isSimilar(fromO.base) && addition.isSimilar(fromO.addition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, addition);
    }
}
