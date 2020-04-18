package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.storage.DropStorage;
import org.bukkit.inventory.ItemStack;

public class PlantInteract {

    private boolean giveBlockDrops = false;
    private boolean goToNext = false;
    private String goToStage;
    private boolean consumeItem = false;

    private ItemStack itemStack;
    private DropStorage dropStorage = new DropStorage();

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


    public boolean isConsumeItem() {
        return consumeItem;
    }

    public void setConsumeItem(boolean consumeItem) {
        this.consumeItem = consumeItem;
    }
}
