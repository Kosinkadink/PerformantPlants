package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;

public class PlantHealEffect extends PlantEffect {

    private ScriptBlock healAmount = ScriptResult.ZERO;

    public PlantHealEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        context.getPlayer().setHealth(Math.max(0, Math.min(20, context.getPlayer().getHealth() + getHealAmountValue(context))));
    }

    public ScriptBlock getHealAmount() {
        return healAmount;
    }

    public double getHealAmountValue(ExecutionContext context) {
        return healAmount.loadValue(context).getDoubleValue();
    }

    public void setHealAmount(ScriptBlock healAmount) {
        this.healAmount = healAmount;
    }

}
