package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlantAirEffect extends PlantEffect {

    private int air = 0;

    public PlantAirEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        player.setRemainingAir(
                Math.min(player.getMaximumAir(),
                        Math.max(0, player.getRemainingAir() + air))
        );
    }

    public int getAir() {
        return air;
    }

    public void setAir(int air) {
        this.air = air;
    }
}
