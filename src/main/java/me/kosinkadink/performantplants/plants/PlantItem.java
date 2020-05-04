package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import org.bukkit.inventory.ItemStack;

public class PlantItem {

    private String id = "";
    private ItemStack itemStack;
    private double buyPrice;
    private double sellPrice;
    private PlantConsumableStorage consumable;

    public PlantItem(ItemStack itemStack, double buyPrice, double sellPrice) {
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public PlantItem(ItemStack itemStack) {
        this(itemStack, -1, -1);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public boolean isConsumable() {
        return consumable != null;
    }

    public PlantConsumableStorage getConsumableStorage() {
        return consumable;
    }

    public void setConsumableStorage(PlantConsumableStorage consumable) {
        this.consumable = consumable;
    }

}
