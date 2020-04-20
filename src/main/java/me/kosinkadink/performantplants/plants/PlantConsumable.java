package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantConsumable {

    private boolean takeItem = true;
    private boolean missingFood = false;
    private ItemStack itemToAdd;
    private ArrayList<RequiredItem> requiredItems = new ArrayList<>();
    private PlantEffectStorage effectStorage = new PlantEffectStorage();

    public PlantConsumable() { }

    public boolean isTakeItem() {
        return takeItem;
    }

    public void setTakeItem(boolean takeItem) {
        this.takeItem = takeItem;
    }

    public boolean isMissingFood() {
        return missingFood;
    }

    public void setMissingFood(boolean missingFood) {
        this.missingFood = missingFood;
    }

    public ItemStack getItemToAdd() {
        return itemToAdd;
    }

    public void addRequiredItem(RequiredItem requiredItem) {
        requiredItems.add(requiredItem);
    }

    public ArrayList<RequiredItem> getRequiredItems() {
        return requiredItems;
    }

    public void setItemToAdd(ItemStack itemToAdd) {
        this.itemToAdd = itemToAdd;
    }

    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

}
