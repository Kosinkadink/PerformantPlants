package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class PlantInteract {

    private ScriptBlock giveBlockDrops = ScriptResult.FALSE;
    private ScriptBlock doIf;
    private ScriptBlock takeItem = ScriptResult.FALSE;
    private ScriptBlock onlyTakeItemOnDo = ScriptResult.FALSE;
    private ScriptBlock breakBlock = ScriptResult.NULL; // defaults to false from getter
    private ScriptBlock onlyBreakBlockOnDo = ScriptResult.NULL; // defaults to false from getter
    private ScriptBlock onlyEffectsOnDo = ScriptResult.TRUE;
    private ScriptBlock onlyConsumableEffectsOnDo = ScriptResult.FALSE;
    private ScriptBlock onlyDropOnDo = ScriptResult.TRUE;

    private boolean matchMaterial = false;
    private boolean matchEnchantments = false;
    private boolean matchEnchantmentLevel = false;

    private HashSet<BlockFace> requiredBlockFaces = new HashSet<>();

    private ItemStack itemStack;
    private DropStorage dropStorage = new DropStorage();
    private PlantEffectStorage effectStorage = new PlantEffectStorage();
    private PlantConsumableStorage consumable;
    private ScriptBlock scriptBlock;
    private ScriptBlock scriptBlockOnDo;
    private ScriptBlock scriptBlockOnNotDo;

    public PlantInteract() { }

    public PlantInteract(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    // drop storage
    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }

    // effect storage
    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

    public void setEffectStorage(PlantEffectStorage effectStorage) {
        this.effectStorage = effectStorage;
    }

    // consumable storage
    public PlantConsumableStorage getConsumableStorage() {
        return consumable;
    }

    public void setConsumableStorage(PlantConsumableStorage consumable) {
        this.consumable = consumable;
    }

    // block drops
    public boolean isGiveBlockDrops(Player player, PlantBlock plantBlock) {
        return giveBlockDrops.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setGiveBlockDrops(ScriptBlock giveBlockDrops) {
        this.giveBlockDrops = giveBlockDrops;
    }

    public boolean isOnlyDropOnDo(Player player, PlantBlock plantBlock) {
        return onlyDropOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyDropOnDo(ScriptBlock onlyDropOnDo) {
        this.onlyDropOnDo = onlyDropOnDo;
    }


    // item stack
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
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

    public void setOnlyTakeItemOnDo(ScriptBlock takeItem) {
        this.onlyTakeItemOnDo = takeItem;
    }

    // break block
    public boolean isBreakBlock(Player player, PlantBlock plantBlock) {
        return isBreakBlockNull() ? false : breakBlock.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setBreakBlock(ScriptBlock breakBlock) {
        this.breakBlock = breakBlock;
    }

    public boolean isBreakBlockNull() {
        return breakBlock == ScriptResult.NULL;
    }

    public boolean isOnlyBreakBlockOnDo(Player player, PlantBlock plantBlock) {
        return isOnlyBreakBlockOnDoNull() ? false : onlyBreakBlockOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyBreakBlockOnDo(ScriptBlock breakBlock) {
        this.onlyBreakBlockOnDo = breakBlock;
    }

    public boolean isOnlyBreakBlockOnDoNull() {
        return onlyBreakBlockOnDo == ScriptResult.NULL;
    }

    // only effects on do
    public boolean isOnlyEffectsOnDo(Player player, PlantBlock plantBlock) {
        return onlyEffectsOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyEffectsOnDo(ScriptBlock onlyEffectsOnDo) {
        this.onlyEffectsOnDo = onlyEffectsOnDo;
    }

    public boolean isOnlyConsumableEffectsOnDo(Player player, PlantBlock plantBlock) {
        return onlyConsumableEffectsOnDo.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setOnlyConsumableEffectsOnDo(ScriptBlock onlyEffectsOnDo) {
        this.onlyConsumableEffectsOnDo = onlyEffectsOnDo;
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

    // matching
    public boolean isMatchMaterial() {
        return matchMaterial;
    }

    public void setMatchMaterial(boolean matchMaterial) {
        this.matchMaterial = matchMaterial;
    }

    public boolean isMatchEnchantments() {
        return matchEnchantments;
    }

    public void setMatchEnchantments(boolean matchEnchantments) {
        this.matchEnchantments = matchEnchantments;
    }

    public boolean isMatchEnchantmentLevel() {
        return matchEnchantmentLevel;
    }

    public void setMatchEnchantmentLevel(boolean matchEnchantmentLevel) {
        this.matchEnchantmentLevel = matchEnchantmentLevel;
    }

    public HashSet<BlockFace> getRequiredBlockFaces() {
        return requiredBlockFaces;
    }

    public void setRequiredBlockFaces(HashSet<BlockFace> blockFaces) {
        requiredBlockFaces = blockFaces;
    }

    public void addRequiredBlockFace(BlockFace blockFace) {
        requiredBlockFaces.add(blockFace);
    }

    public boolean hasRequiredBlockFaces() {
        return !requiredBlockFaces.isEmpty();
    }

    public boolean isRequiredBlockFace(BlockFace blockFace) {
        return requiredBlockFaces.contains(blockFace);
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
