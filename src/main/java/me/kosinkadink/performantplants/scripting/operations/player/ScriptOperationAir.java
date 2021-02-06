package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;

import javax.annotation.Nonnull;

public class ScriptOperationAir extends ScriptOperationPlayer {

    public ScriptOperationAir(ScriptBlock amount) {
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
            context.getPlayer().setRemainingAir(Math.min(context.getPlayer().getMaximumAir(),
                    Math.max(0, context.getPlayer().getRemainingAir() + amount)));
            return new ScriptResult(context.getPlayer().getRemainingAir());
        }
        return ScriptResult.ZERO;
    }

    @Override
    protected void setType() {
        type = ScriptType.DOUBLE;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isLong(getAmount())) {
            throw new IllegalArgumentException(String.format("amount must be ScriptType LONG, not %s", getAmount().getType()));
        }
    }
}
