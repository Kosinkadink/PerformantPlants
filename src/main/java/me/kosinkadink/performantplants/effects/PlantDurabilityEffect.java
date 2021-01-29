package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.ItemHelper;

public class PlantDurabilityEffect extends PlantEffect {

    private ScriptBlock amount = ScriptResult.ZERO;

    public PlantDurabilityEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        ItemHelper.updateDamage(context.getPlayer().getInventory().getItemInMainHand(), getAmountValue(context));
    }

    public ScriptBlock getAmount() {
        return amount;
    }

    public int getAmountValue(ExecutionContext context) {
        return amount.loadValue(context).getIntegerValue();
    }

    public void setAmount(ScriptBlock amount) {
        this.amount = amount;
    }

}
