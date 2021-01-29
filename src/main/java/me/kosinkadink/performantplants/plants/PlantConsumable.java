package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlantConsumable {

    private ScriptBlock takeItem = ScriptResult.TRUE;
    private ScriptBlock onlyTakeItemOnDo = ScriptResult.TRUE;

    private ScriptBlock missingFood = ScriptResult.FALSE;
    private ScriptBlock normalEat = ScriptResult.TRUE;

    private ScriptBlock addDamage = ScriptResult.ZERO;
    private ScriptBlock onlyAddDamageOnDo = ScriptResult.TRUE;

    private final ArrayList<ItemStack> itemsToGive = new ArrayList<>();
    private ScriptBlock onlyGiveItemsOnDo = ScriptResult.TRUE;

    private final ArrayList<RequiredItem> requiredItems = new ArrayList<>();
    private ScriptBlock onlyTakeRequiredItemsOnDo = ScriptResult.TRUE;

    private ScriptBlock condition = ScriptResult.TRUE;

    private final PlantEffectStorage effectStorage = new PlantEffectStorage();
    private ScriptBlock onlyEffectsOnDo = ScriptResult.TRUE;

    private ScriptBlock doIf;
    private ScriptBlock scriptBlock;
    private ScriptBlock scriptBlockOnDo;
    private ScriptBlock scriptBlockOnNotDo;

    public PlantConsumable() { }

    // condition
    public boolean isConditionMet(ExecutionContext context) {
        return condition.loadValue(context).getBooleanValue();
    }

    public void setCondition(ScriptBlock condition) {
        this.condition = condition;
    }

    // take item
    public boolean isTakeItem(ExecutionContext context) {
        return takeItem.loadValue(context).getBooleanValue();
    }

    public void setTakeItem(ScriptBlock takeItem) {
        this.takeItem = takeItem;
    }

    public boolean isOnlyTakeItemOnDo(ExecutionContext context) {
        return onlyTakeItemOnDo.loadValue(context).getBooleanValue();
    }

    public void setOnlyTakeItemOnDo(ScriptBlock onlyTakeItemOnDo) {
        this.onlyTakeItemOnDo = onlyTakeItemOnDo;
    }

    // missing food
    public boolean isMissingFood(ExecutionContext context) {
        return missingFood.loadValue(context).getBooleanValue();
    }

    public void setMissingFood(ScriptBlock missingFood) {
        this.missingFood = missingFood;
    }

    // normal eat
    public boolean isNormalEat(ExecutionContext context) {
        return normalEat.loadValue(context).getBooleanValue();
    }

    public void setNormalEat(ScriptBlock normalEat) {
        this.normalEat = normalEat;
    }

    // add damage
    public int getAddDamage(ExecutionContext context) {
        return addDamage.loadValue(context).getIntegerValue();
    }

    public void setAddDamage(ScriptBlock addDamage) {
        this.addDamage = addDamage;
    }

    public boolean isOnlyAddDamageOnDo(ExecutionContext context) {
        return onlyAddDamageOnDo.loadValue(context).getBooleanValue();
    }

    public void setOnlyAddDamageOnDo(ScriptBlock onlyAddDamageOnDo) {
        this.onlyAddDamageOnDo = onlyAddDamageOnDo;
    }

    // items to give
    public void addItemToGive(ItemStack itemToAdd) {
        itemsToGive.add(itemToAdd);
    }

    public ArrayList<ItemStack> getItemsToGive() {
        return itemsToGive;
    }

    public boolean isOnlyGiveItemsOnDo(ExecutionContext context) {
        return onlyGiveItemsOnDo.loadValue(context).getBooleanValue();
    }

    public void setOnlyGiveItemsOnDo(ScriptBlock onlyGiveItemsOnDo) {
        this.onlyGiveItemsOnDo = onlyGiveItemsOnDo;
    }

    // required items
    public void addRequiredItem(RequiredItem requiredItem) {
        requiredItems.add(requiredItem);
    }

    public ArrayList<RequiredItem> getRequiredItems() {
        return requiredItems;
    }

    public boolean isOnlyTakeRequiredItemsOnDo(ExecutionContext context) {
        return onlyTakeRequiredItemsOnDo.loadValue(context).getBooleanValue();
    }

    public void setOnlyTakeRequiredItemsOnDo(ScriptBlock onlyTakeRequiredItemsOnDo) {
        this.onlyTakeRequiredItemsOnDo = onlyTakeRequiredItemsOnDo;
    }

    // effect storage
    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

    // only effects on do
    public boolean isOnlyEffectsOnDo(ExecutionContext context) {
        return onlyEffectsOnDo.loadValue(context).getBooleanValue();
    }

    public void setOnlyEffectsOnDo(ScriptBlock onlyEffectsOnDo) {
        this.onlyEffectsOnDo = onlyEffectsOnDo;
    }

    // do if
    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public boolean generateDoIf(ExecutionContext context) {
        if (doIf != null) {
            return doIf.loadValue(context).getBooleanValue();
        }
        return true;
    }

    // script blocks
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
