package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import org.bukkit.entity.Player;

public class PlantHealEffect extends PlantEffect {

    private double healAmount = 0.0;

    public PlantHealEffect() { }

    public PlantHealEffect(int healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.setHealth(Math.max(0, Math.min(20, player.getHealth() + healAmount)));
    }

    public double getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(double healAmount) {
        this.healAmount = healAmount;
    }

}
