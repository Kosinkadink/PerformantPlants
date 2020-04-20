package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlantHealEffect extends PlantEffect {

    private double healAmount = 0.0;

    public PlantHealEffect() { }

    public PlantHealEffect(int healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    void performEffectAction(Player player, Location location) {
        player.setHealth(Math.min(20, player.getHealth() + healAmount));
    }

    public double getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(double healAmount) {
        this.healAmount = healAmount;
    }

}
