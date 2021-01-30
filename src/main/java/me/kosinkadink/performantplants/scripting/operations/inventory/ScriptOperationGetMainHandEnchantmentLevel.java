package me.kosinkadink.performantplants.scripting.operations.inventory;

import me.kosinkadink.performantplants.scripting.*;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

public class ScriptOperationGetMainHandEnchantmentLevel extends ScriptOperation {

    public ScriptOperationGetMainHandEnchantmentLevel(ScriptBlock enchantment) {
        super(enchantment);
    }

    public ScriptBlock getEnchantment() {
        return inputs[0];
    }

    @Override
    public @Nonnull ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        String enchantmentName = getEnchantment().loadValue(context).getStringValue().toLowerCase();
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));
        } catch (IllegalArgumentException ignored) { }
        if (enchantment == null || !context.isPlayerSet()) {
            return ScriptResult.ZERO;
        }
        return new ScriptResult(context.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(enchantment));
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    public boolean shouldOptimize() {
        return false;
    }

    @Override
    public @Nonnull ScriptCategory getCategory() {
        return ScriptCategory.INVENTORY;
    }
}
