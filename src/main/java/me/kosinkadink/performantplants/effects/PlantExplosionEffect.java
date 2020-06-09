package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantExplosionEffect extends PlantEffect {

    private ScriptBlock power = new ScriptResult(1.0);
    private ScriptBlock fire = ScriptResult.FALSE;
    private ScriptBlock breakBlocks = ScriptResult.FALSE;

    public PlantExplosionEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.getWorld().createExplosion(
                player.getLocation(),
                getPowerValue(player, plantBlock),
                isFire(player, plantBlock),
                isBreakBlocks(player, plantBlock)
        );
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        block.getWorld().createExplosion(
                BlockHelper.getCenter(block),
                getPowerValue(null, plantBlock),
                isFire(null, plantBlock),
                isBreakBlocks(null, plantBlock)
        );
    }

    public ScriptBlock getPower() {
        return power;
    }

    public float getPowerValue(Player player, PlantBlock plantBlock) {
        return power.loadValue(plantBlock, player).getFloatValue();
    }

    public void setPower(ScriptBlock power) {
        this.power = power;
    }

    public ScriptBlock getFire() {
        return fire;
    }

    public boolean isFire(Player player, PlantBlock plantBlock) {
        return fire.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setFire(ScriptBlock fire) {
        this.fire = fire;
    }

    public ScriptBlock getBreakBlocks() {
        return breakBlocks;
    }

    public boolean isBreakBlocks(Player player, PlantBlock plantBlock) {
        return breakBlocks.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setBreakBlocks(ScriptBlock breakBlocks) {
        this.breakBlocks = breakBlocks;
    }
}
