package me.kosinkadink.performantplants.recipes.keys;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemStackRecipeKey {

    private final ItemStack itemStack;

    public ItemStackRecipeKey(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    public ItemStack getItemStack() {
        return itemStack;
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
        ItemStackRecipeKey fromO = (ItemStackRecipeKey)o;
        // true if components match, false otherwise
        return itemStack.isSimilar(fromO.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack);
    }

}
