package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;

import javax.annotation.Nonnull;

public class ScriptOperationFeed extends ScriptOperationPlayer {

    public ScriptOperationFeed(ScriptBlock amount) {
        super(amount);
    }

    public ScriptBlock getAmount() {
        return inputs[0];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            int amount = getAmount().loadValue(context).getIntegerValue();
            int newFoodLevel = Math.max(0, Math.min(20, context.getPlayer().getFoodLevel() + amount));
            // int player
            return new ScriptResult(context.getPlayer().getHealth());
        }
        return ScriptResult.ZERO;
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }
}
