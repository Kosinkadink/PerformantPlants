package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.effects.PlantEffect;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantConsumable {

    private boolean decrementItem = true;
    private ItemStack itemStackToAdd;
    private ArrayList<PlantEffect> effects = new ArrayList<>();

    public PlantConsumable() { }

    public boolean isDecrementItem() {
        return decrementItem;
    }

    public void setDecrementItem(boolean decrementItem) {
        this.decrementItem = decrementItem;
    }

    public ItemStack getItemStackToAdd() {
        return itemStackToAdd;
    }

    public void setItemStackToAdd(ItemStack itemStackToAdd) {
        this.itemStackToAdd = itemStackToAdd;
    }

    public void addEffect(PlantEffect effect) {
        effects.add(effect);
    }

    public ArrayList<PlantEffect> getEffects() {
        return effects;
    }

}
