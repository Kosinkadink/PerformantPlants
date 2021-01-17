package me.kosinkadink.performantplants.recipes.keys;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class AnvilRecipeKey extends RecipeKey {

    private final ItemStack base;
    private final ItemStack addition;
    private final String name;


    public AnvilRecipeKey(ItemStack base, ItemStack addition, String name) {
        this.base = base.clone();
        this.addition = addition.clone();
        this.name = name;
        this.base.setAmount(1);
        this.addition.setAmount(1);
    }

    public ItemStack getBase() {
        return base;
    }

    public ItemStack getAddition() {
        return addition;
    }

    public String getName() {
        return name;
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
        AnvilRecipeKey fromO = (AnvilRecipeKey)o;
        // true if components match, false otherwise
        return base.isSimilar(fromO.base) && addition.isSimilar(fromO.addition) && name.equals(fromO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, addition, name);
    }

}
