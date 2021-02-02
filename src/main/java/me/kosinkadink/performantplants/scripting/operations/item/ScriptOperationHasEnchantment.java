package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ScriptOperationHasEnchantment extends ScriptOperationItem {

    public ScriptOperationHasEnchantment(ScriptBlock itemStack, ScriptBlock enchantment) {
        super(itemStack, enchantment);
    }

    public ScriptBlock getItemStack() {
        return inputs[0];
    }

    public ScriptBlock getEnchantment() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        // get item stack
        ItemStack itemStack = getItemStack().loadValue(context).getItemStackValue();
        // get enchantment
        String enchantmentName = getEnchantment().loadValue(context).getStringValue().toLowerCase();
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));
        } catch (IllegalArgumentException ignored) { }
        if (enchantment == null) {
            return ScriptResult.FALSE;
        }
        return new ScriptResult(itemStack.containsEnchantment(enchantment));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isItemStack(getItemStack())) {
            throw new IllegalArgumentException("Requires ScriptType ITEMSTACK for itemstack");
        }
        if (!ScriptHelper.isString(getEnchantment())) {
            throw new IllegalArgumentException("Requires ScriptType STRING for enchantment");
        }
    }
}
