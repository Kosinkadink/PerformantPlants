package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
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
    void performEffectActionPlayer(ExecutionContext context) {
        Player player = context.getPlayer();
        player.getWorld().createExplosion(
                player.getLocation(),
                getPowerValue(context),
                isFire(context),
                isBreakBlocks(context)
        );
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        Block block = context.getPlantBlock().getBlock();
        block.getWorld().createExplosion(
                BlockHelper.getCenter(block),
                getPowerValue(context),
                isFire(context),
                isBreakBlocks(context)
        );
    }

    public ScriptBlock getPower() {
        return power;
    }

    public float getPowerValue(ExecutionContext context) {
        return power.loadValue(context).getFloatValue();
    }

    public void setPower(ScriptBlock power) {
        this.power = power;
    }

    public ScriptBlock getFire() {
        return fire;
    }

    public boolean isFire(ExecutionContext context) {
        return fire.loadValue(context).getBooleanValue();
    }

    public void setFire(ScriptBlock fire) {
        this.fire = fire;
    }

    public ScriptBlock getBreakBlocks() {
        return breakBlocks;
    }

    public boolean isBreakBlocks(ExecutionContext context) {
        return breakBlocks.loadValue(context).getBooleanValue();
    }

    public void setBreakBlocks(ScriptBlock breakBlocks) {
        this.breakBlocks = breakBlocks;
    }
}
