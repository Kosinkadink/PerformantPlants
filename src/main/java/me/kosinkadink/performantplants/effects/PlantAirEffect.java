package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class PlantAirEffect extends PlantEffect {

    private ScriptBlock amount = ScriptResult.ZERO;

    public PlantAirEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.setRemainingAir(
                Math.min(player.getMaximumAir(),
                        Math.max(0, player.getRemainingAir() + getAmountValue(player, plantBlock)))
        );
    }

    public ScriptBlock getAmount() {
        return amount;
    }

    public int getAmountValue(Player player, PlantBlock plantBlock) {
        return amount.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setAmount(ScriptBlock amount) {
        this.amount = amount;
    }
}
