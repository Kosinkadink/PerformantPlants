package me.kosinkadink.performantplants.settings;

public class DropSettings {

    private int minAmount = 1;
    private int maxAmount = 1;
    private double chance = 100.0;
    private ItemSettings itemSettings;

    public DropSettings() {}


    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public ItemSettings getItemSettings() {
        return itemSettings;
    }

    public void setItemSettings(ItemSettings itemSettings) {
        this.itemSettings = itemSettings;
    }
}
