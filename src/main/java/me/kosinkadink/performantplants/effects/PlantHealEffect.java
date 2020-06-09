package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.entity.Player;

public class PlantHealEffect extends PlantEffect {

    private ScriptBlock healAmount = ScriptResult.ZERO;

    public PlantHealEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.setHealth(Math.max(0, Math.min(20, player.getHealth() + getHealAmountValue(player, plantBlock))));
    }

    public ScriptBlock getHealAmount() {
        return healAmount;
    }

    public double getHealAmountValue(Player player, PlantBlock plantBlock) {
        return healAmount.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setHealAmount(ScriptBlock healAmount) {
        this.healAmount = healAmount;
    }

}
