package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.entity.Player;
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
    public boolean isConditionMet(Player player, PlantBlock plantBlock) {
        return condition.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setCondition(ScriptBlock condition) {
        this.condition = condition;
    }

    // take item
    public boolean isTakeItem(Player player, PlantBlock plantBlock) {
        return takeItem.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setTakeItem(ScriptBlock takeItem) {
        this.takeItem = takeItem;
    }

    public boolean isOnlyTakeItemOnDo(Player player, PlantBlock plantBlock) {
        return onlyTakeItemOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyTakeItemOnDo(ScriptBlock onlyTakeItemOnDo) {
        this.onlyTakeItemOnDo = onlyTakeItemOnDo;
    }

    // missing food
    public boolean isMissingFood(Player player, PlantBlock plantBlock) {
        return missingFood.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setMissingFood(ScriptBlock missingFood) {
        this.missingFood = missingFood;
    }

    // normal eat
    public boolean isNormalEat(Player player, PlantBlock plantBlock) {
        return normalEat.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setNormalEat(ScriptBlock normalEat) {
        this.normalEat = normalEat;
    }

    // add damage
    public int getAddDamage(Player player, PlantBlock plantBlock) {
        return addDamage.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setAddDamage(ScriptBlock addDamage) {
        this.addDamage = addDamage;
    }

    public boolean isOnlyAddDamageOnDo(Player player, PlantBlock plantBlock) {
        return onlyAddDamageOnDo.loadValue(plantBlock, player).getBooleanValue();
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

    public boolean isOnlyGiveItemsOnDo(Player player, PlantBlock plantBlock) {
        return onlyGiveItemsOnDo.loadValue(plantBlock, player).getBooleanValue();
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

    public boolean isOnlyTakeRequiredItemsOnDo(Player player, PlantBlock plantBlock) {
        return onlyTakeRequiredItemsOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyTakeRequiredItemsOnDo(ScriptBlock onlyTakeRequiredItemsOnDo) {
        this.onlyTakeRequiredItemsOnDo = onlyTakeRequiredItemsOnDo;
    }

    // effect storage
    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

    // only effects on do
    public boolean isOnlyEffectsOnDo(Player player, PlantBlock plantBlock) {
        return onlyEffectsOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyEffectsOnDo(ScriptBlock onlyEffectsOnDo) {
        this.onlyEffectsOnDo = onlyEffectsOnDo;
    }

    // do if
    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public boolean generateDoIf(Player player, PlantBlock plantBlock) {
        if (doIf != null) {
            return doIf.loadValue(plantBlock, player).getBooleanValue();
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
