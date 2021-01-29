package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class PlantAirEffect extends PlantEffect {

    private ScriptBlock amount = ScriptResult.ZERO;

    public PlantAirEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        Player player = context.getPlayer();
        player.setRemainingAir(
                Math.min(player.getMaximumAir(),
                        Math.max(0, player.getRemainingAir() + getAmountValue(context)))
        );
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
