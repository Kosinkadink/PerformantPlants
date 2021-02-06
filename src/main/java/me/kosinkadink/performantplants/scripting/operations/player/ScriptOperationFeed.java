package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ScriptOperationFeed extends ScriptOperationPlayer {

    public ScriptOperationFeed(ScriptBlock foodAmount, ScriptBlock saturateAmount) {
        super(foodAmount, saturateAmount);
    }

    public ScriptBlock getFoodAmount() {
        return inputs[0];
    }

    public ScriptBlock getSaturateAmount() {
        return inputs[1];
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            Player player = context.getPlayer();
            int newFoodLevel = Math.max(0, Math.min(20, player.getFoodLevel() + getFoodAmount().loadValue(context).getIntegerValue()));
            float newSaturationLevel = Math.max(0, Math.min(newFoodLevel, player.getSaturation() + getSaturateAmount().loadValue(context).getFloatValue()));
            player.setFoodLevel(newFoodLevel);
            player.setSaturation(newSaturationLevel);
            // int player
            return new ScriptResult(player.getFoodLevel());
        }
        return ScriptResult.ZERO;
    }

    @Override
    protected void setType() {
        type = ScriptType.LONG;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isLong(getFoodAmount())) {
            throw new IllegalArgumentException(String.format("food-amount must be ScriptType LONG, not %s", getFoodAmount().getType()));
        }
        if (!ScriptHelper.isNumeric(getSaturateAmount())) {
            throw new IllegalArgumentException(String.format("saturate-amount must be ScriptType LONG or DOUBLE, not %s", getFoodAmount().getType()));
        }
    }
}
