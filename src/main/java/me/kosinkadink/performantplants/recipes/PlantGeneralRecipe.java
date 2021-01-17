package me.kosinkadink.performantplants.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class PlantGeneralRecipe implements Recipe {

    protected ItemStack result;
    protected NamespacedKey namespacedKey;

    @Override
    public ItemStack getResult() {
        return result;
    }

    public NamespacedKey getKey() {
        return namespacedKey;
    }

}
