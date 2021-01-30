package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

public class ScriptOperationItemGetEnchantmentLevel extends ScriptOperationItem {

    public ScriptOperationItemGetEnchantmentLevel(ScriptBlock enchantment) {
        super(enchantment);
    }

    public ScriptBlock getEnchantment() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (!context.isItemStackSet()) {
            return ScriptResult.ZERO;
        }
        String enchantmentName = getEnchantment().loadValue(context).getStringValue().toLowerCase();
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));
        } catch (IllegalArgumentException ignored) { }
        if (enchantment == null) {
            return ScriptResult.ZERO;
        }
        return new ScriptResult(context.getItemStack().getEnchantmentLevel(enchantment));
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }
}
