package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.storage.DropStorage;
import me.kosinkadink.performantplants.storage.PlantEffectStorage;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class PlantInteract {

    private boolean giveBlockDrops = false;
    private boolean goToNext = false;
    private String goToStage;
    private boolean takeItem = false;
    private double chance = 100.0;

    private ItemStack itemStack;
    private DropStorage dropStorage = new DropStorage();
    private PlantEffectStorage effectStorage = new PlantEffectStorage();

    public PlantInteract() { }

    public PlantInteract(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public DropStorage getDropStorage() {
        return dropStorage;
    }

    public void setDropStorage(DropStorage dropStorage) {
        this.dropStorage = dropStorage;
    }

    public PlantEffectStorage getEffectStorage() {
        return effectStorage;
    }

    public void setEffectStorage(PlantEffectStorage effectStorage) {
        this.effectStorage = effectStorage;
    }

    public String getGoToStage() {
        return goToStage;
    }

    public boolean isChangeStage() {
        return goToStage != null || isGoToNext();
    }

    public void setGoToStage(String goToStage) {
        if (goToStage != null && !goToStage.isEmpty()) {
            this.goToStage = goToStage;
        }
    }

    public boolean isGiveBlockDrops() {
        return giveBlockDrops;
    }

    public void setGiveBlockDrops(boolean giveBlockDrops) {
        this.giveBlockDrops = giveBlockDrops;
    }

    public boolean isGoToNext() {
        return goToNext;
    }

    public void setGoToNext(boolean goToNext) {
        this.goToNext = goToNext;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }


    public boolean isTakeItem() {
        return takeItem;
    }

    public void setTakeItem(boolean takeItem) {
        this.takeItem = takeItem;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        if (chance >= 0.0 && chance <= 100.0) {
            this.chance = chance;
        }
    }

    public boolean generateChance() {
        return ThreadLocalRandom.current().nextDouble() <= chance / 100.0;
    }

}
