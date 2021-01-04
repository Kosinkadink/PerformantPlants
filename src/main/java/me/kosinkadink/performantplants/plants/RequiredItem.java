package me.kosinkadink.performantplants.plants;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RequiredItem {

    private ItemStack itemStack;
    private ScriptBlock takeItem = ScriptResult.FALSE;
    private ScriptBlock inHand = ScriptResult.FALSE;
    private ScriptBlock addDamage = ScriptResult.ZERO;

    public RequiredItem(ItemStack itemStack) {
        this.itemStack = itemStack;
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

    // in hand
    public boolean isInHand(Player player, PlantBlock plantBlock) {
        return inHand.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setInHand(ScriptBlock inHand) {
        this.inHand = inHand;
    }

    // add damage
    public int getAddDamage(Player player, PlantBlock plantBlock) {
        return addDamage.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setAddDamage(ScriptBlock addDamage) {
        this.addDamage = addDamage;
    }
}
