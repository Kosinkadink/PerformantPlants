package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.storage.PlantConsumableStorage;
import org.bukkit.inventory.ItemStack;

public class PlantItem {

    private String id = "";
    private ItemStack itemStack;
    private double buyPrice;
    private double sellPrice;
    private PlantConsumableStorage consumable;
    private PlantConsumableStorage clickable;
    private int burnTime = 0;

    private boolean vanillaItem = false;

    private boolean allowAnvil = false;
    private boolean allowAnvilRename = false;
    private boolean allowSmithing = false;
    private boolean allowGrindstone = false;
    private boolean allowStonecutter = false;
    private boolean allowEnchanting = false;
    private boolean allowBeacon = false;
    private boolean allowLoom = false;
    private boolean allowCartography = false;
    private boolean allowCrafting = false;
    private boolean allowFuel = false;
    private boolean allowBrewing = false;

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

    // vanilla item
    public boolean isVanillaItem() {
        return vanillaItem;
    }

    public void setVanillaItem(boolean vanillaItem) {
        this.vanillaItem = vanillaItem;
    }

    //region Prices
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
    //endregion

    //region Click Interaction
    public boolean isConsumable() {
        return consumable != null;
    }

    public PlantConsumableStorage getConsumableStorage() {
        return consumable;
    }

    public void setConsumableStorage(PlantConsumableStorage consumable) {
        this.consumable = consumable;
    }

    public boolean isClickable() {
        return clickable != null;
    }

    public PlantConsumableStorage getClickableStorage() {
        return clickable;
    }

    public void setClickableStorage(PlantConsumableStorage clickable) {
        this.clickable = clickable;
    }
    //endregion

    //region Burn Time
    public int getBurnTime() {
        return burnTime;
    }

    public boolean hasBurnTime() {
        return burnTime > 0;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = Math.max(0, burnTime);
    }
    //endregion

    //region Allow Usage
    public boolean isAllowAnvil() {
        return allowAnvil;
    }

    public void setAllowAnvil(boolean allowAnvil) {
        this.allowAnvil = allowAnvil;
    }

    public boolean isAllowAnvilRename() {
        return allowAnvilRename;
    }

    public void setAllowAnvilRename(boolean allowAnvilRename) {
        this.allowAnvilRename = allowAnvilRename;
    }

    public boolean isAllowSmithing() {
        return allowSmithing;
    }

    public void setAllowSmithing(boolean allowSmithing) {
        this.allowSmithing = allowSmithing;
    }

    public boolean isAllowGrindstone() {
        return allowGrindstone;
    }

    public void setAllowGrindstone(boolean allowGrindstone) {
        this.allowGrindstone = allowGrindstone;
    }

    public boolean isAllowStonecutter() {
        return allowStonecutter;
    }

    public void setAllowStonecutter(boolean allowStonecutter) {
        this.allowStonecutter = allowStonecutter;
    }

    public boolean isAllowEnchanting() {
        return allowEnchanting;
    }

    public void setAllowEnchanting(boolean allowEnchanting) {
        this.allowEnchanting = allowEnchanting;
    }

    public boolean isAllowBeacon() {
        return allowBeacon;
    }

    public void setAllowBeacon(boolean allowBeacon) {
        this.allowBeacon = allowBeacon;
    }

    public boolean isAllowLoom() {
        return allowLoom;
    }

    public void setAllowLoom(boolean allowLoom) {
        this.allowLoom = allowLoom;
    }

    public boolean isAllowCartography() {
        return allowCartography;
    }

    public void setAllowCartography(boolean allowCartography) {
        this.allowCartography = allowCartography;
    }

    public boolean isAllowCrafting() {
        return allowCrafting;
    }

    public void setAllowCrafting(boolean allowCrafting) {
        this.allowCrafting = allowCrafting;
    }

    public boolean isAllowFuel() {
        return allowFuel;
    }

    public void setAllowFuel(boolean allowFuel) {
        this.allowFuel = allowFuel;
    }

    public boolean isAllowBrewing() {
        return allowBrewing;
    }

    public void setAllowBrewing(boolean allowBrewing) {
        this.allowBrewing = allowBrewing;
    }
    //endregion

}
