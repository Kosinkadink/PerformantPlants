package me.kosinkadink.performantplants.plants;

import org.bukkit.inventory.ItemStack;

public class RequiredItem {

    private ItemStack itemStack;
    private boolean takeItem = false;
    private boolean inHand = false;

    public RequiredItem(ItemStack itemStack) {
        this.itemStack = itemStack;
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

    public boolean isInHand() {
        return inHand;
    }

    public void setInHand(boolean inHand) {
        this.inHand = inHand;
    }

}
