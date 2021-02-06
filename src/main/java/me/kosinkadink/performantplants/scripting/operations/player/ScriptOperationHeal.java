package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationHeal extends ScriptOperationPlayer {

    public ScriptOperationHeal(ScriptBlock amount) {
        super(amount);
    }

    public ScriptBlock getAmount() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            double amount = getAmount().loadValue(context).getDoubleValue();
            context.getPlayer().setHealth(Math.max(0, Math.min(20, context.getPlayer().getHealth() + amount)));
            return new ScriptResult(context.getPlayer().getHealth());
        }
        return ScriptResult.ZERO;
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }
}
