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
    private final ArrayList<ItemStack> itemsToGive = new ArrayList<>();
    private final ArrayList<RequiredItem> requiredItems = new ArrayList<>();
    private final PlantEffectStorage effectStorage = new PlantEffectStorage();

    private ScriptBlock doIf;
    private ScriptBlock scriptBlock;
    private ScriptBlock scriptBlockOnDo;
    private ScriptBlock scriptBlockOnNotDo;

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

    // script blocks
    public ScriptBlock getDoIf() {
        return doIf;
    }

    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public ScriptBlock getScriptBlock() {
        return scriptBlock;
    }

    public void setScriptBlock(ScriptBlock scriptBlock) {
        this.scriptBlock = scriptBlock;
    }

    public ScriptBlock getScriptBlockOnDo() {
        return scriptBlockOnDo;
    }

    public void setScriptBlockOnDo(ScriptBlock scriptBlockOnDo) {
        this.scriptBlockOnDo = scriptBlockOnDo;
    }

    public ScriptBlock getScriptBlockOnNotDo() {
        return scriptBlockOnNotDo;
    }

    public void setScriptBlockOnNotDo(ScriptBlock scriptBlockOnNotDo) {
        this.scriptBlockOnNotDo = scriptBlockOnNotDo;
    }
}
