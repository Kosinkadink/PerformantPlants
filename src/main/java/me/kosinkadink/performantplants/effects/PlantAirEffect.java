package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlantAirEffect extends PlantEffect {

    private int amount = 0;

    public PlantAirEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        player.setRemainingAir(
                Math.min(player.getMaximumAir(),
                        Math.max(0, player.getRemainingAir() + amount))
        );
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
