package me.kosinkadink.performantplants.recipes.keys;

abstract public class RecipeKey {

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

}
