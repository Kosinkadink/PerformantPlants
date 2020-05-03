package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantExplosionEffect extends PlantEffect {

    private float power = 1.0F;
    private boolean fire = false;
    private boolean breakBlocks = false;

    public PlantExplosionEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        player.getWorld().createExplosion(player.getLocation(), power, fire, breakBlocks);
    }

    @Override
    void performEffectAction(Block block) {
        block.getWorld().createExplosion(BlockHelper.getCenter(block), power, fire, breakBlocks);
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public boolean isFire() {
        return fire;
    }

    public void setFire(boolean fire) {
        this.fire = fire;
    }

    public boolean isBreakBlocks() {
        return breakBlocks;
    }

    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
    }
}
