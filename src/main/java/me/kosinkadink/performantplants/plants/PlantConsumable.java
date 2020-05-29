package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantConsumable {

    private boolean takeItem = true;
    private boolean missingFood = false;
    private boolean normalEat = true;
    private int addDamage = 0;
    private ArrayList<ItemStack> itemsToGive = new ArrayList<>();
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

    public boolean isNormalEat() {
        return normalEat;
    }

    public void setNormalEat(boolean normalEat) {
        this.normalEat = normalEat;
    }

    public int getAddDamage() {
        return addDamage;
    }

    public void setAddDamage(int addDamage) {
        this.addDamage = addDamage;
    }

    public void addItemToGive(ItemStack itemToAdd) {
        itemsToGive.add(itemToAdd);
    }

    public ArrayList<ItemStack> getItemsToGive() {
        return itemsToGive;
    }

    public void addRequiredItem(RequiredItem requiredItem) {
        requiredItems.add(requiredItem);
    }

    public ArrayList<RequiredItem> getRequiredItems() {
        return requiredItems;
    }

    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

}
