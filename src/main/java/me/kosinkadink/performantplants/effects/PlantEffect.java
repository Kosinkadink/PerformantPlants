package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class PlantEffect {

    protected ScriptBlock doIf = ScriptResult.TRUE;
    protected ScriptBlock delay = ScriptResult.ZERO;

    public boolean performEffect(Player player, PlantBlock plantBlock) {
        if (getDoIfValue(player, plantBlock)) {
            int delayValue = getDelayValue(player, plantBlock);
            if (delayValue == 0) {
                performEffectAction(player, plantBlock);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectAction(player, plantBlock),
                            delayValue
                    );
                } else {
                    Bukkit.getLogger().warning("Could not schedule performEffectAction;" +
                            "performantplants plugin not found");
                }
            }
            return true;
        }
        return false;
    }

    public boolean performEffect(Block block, PlantBlock plantBlock) {
        if (getDoIfValue(null, plantBlock)) {
            int delayValue = getDelayValue(null, plantBlock);
            if (delayValue == 0) {
                performEffectAction(block, plantBlock);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectAction(block, plantBlock),
                            delayValue
                    );
                } else {
                    Bukkit.getLogger().warning("Could not schedule performEffectAction;" +
                            "performantplants plugin not found");
                }
            }
            return true;
        }
        return false;
    }

    void performEffectAction(Player player, PlantBlock plantBlock) { }

    void performEffectAction(Block block, PlantBlock plantBlock) { }

    public ScriptBlock getDoIf() {
        return doIf;
    }

    public boolean getDoIfValue(Player player, PlantBlock plantBlock) {
        return doIf.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public ScriptBlock getDelay() {
        return delay;
    }

    public int getDelayValue(Player player, PlantBlock plantBlock) {
        return Math.max(0, delay.loadValue(plantBlock, player).getIntegerValue());
    }

    public void setDelay(ScriptBlock delay) {
        this.delay = delay;
    }

}
