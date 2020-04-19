package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantHealEffect implements PlantEffect {

    private double healAmount = 0.0;

    public PlantHealEffect(int healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    public void performEffect(Player player, Location location) {
        player.setHealth(Math.min(20, player.getHealth()));
    }

    @Override
    public void performEffect(Block block) { }

    public double getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(double healAmount) {
        this.healAmount = healAmount;
    }
}
