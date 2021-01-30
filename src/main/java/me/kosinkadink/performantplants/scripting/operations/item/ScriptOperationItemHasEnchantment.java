package me.kosinkadink.performantplants.scripting.operations.item;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

public class ScriptOperationItemHasEnchantment extends ScriptOperationItem {

    public ScriptOperationItemHasEnchantment(ScriptBlock enchantment) {
        super(enchantment);
    }

    public ScriptBlock getEnchantment() {
        return inputs[0];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (!context.isItemStackSet()) {
            return ScriptResult.FALSE;
        }
        String enchantmentName = getEnchantment().loadValue(context).getStringValue().toLowerCase();
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));
        } catch (IllegalArgumentException ignored) { }
        if (enchantment == null) {
            return ScriptResult.FALSE;
        }
        return new ScriptResult(context.getItemStack().containsEnchantment(enchantment));
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }
}
