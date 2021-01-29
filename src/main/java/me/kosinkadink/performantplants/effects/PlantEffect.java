package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class PlantEffect {

    protected ScriptBlock doIf = ScriptResult.TRUE;
    protected ScriptBlock delay = ScriptResult.ZERO;

    public boolean performEffect(ExecutionContext context) {
        if (context.isPlayerSet()) {
            return performEffectPlayer(context);
        } else {
            return performEffectBlock(context);
        }
    }

    public boolean performEffectPlayer(ExecutionContext context) {
        if (getDoIfValue(context)) {
            int delayValue = getDelayValue(context);
            if (delayValue == 0) {
                performEffectActionPlayer(context);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectActionPlayer(context),
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

    public boolean performEffectBlock(ExecutionContext context) {
        if (getDoIfValue(context)) {
            int delayValue = getDelayValue(context);
            if (delayValue == 0) {
                performEffectActionBlock(context);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectActionBlock(context),
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

    void performEffectActionPlayer(ExecutionContext context) { }

    void performEffectActionBlock(ExecutionContext context) { }

    public ScriptBlock getDoIf() {
        return doIf;
    }

    public boolean getDoIfValue(ExecutionContext context) {
        return doIf.loadValue(context).getBooleanValue();
    }

    public void setDoIf(ScriptBlock doIf) {
        this.doIf = doIf;
    }

    public ScriptBlock getDelay() {
        return delay;
    }

    public int getDelayValue(ExecutionContext context) {
        return Math.max(0, delay.loadValue(context).getIntegerValue());
    }

    public void setDelay(ScriptBlock delay) {
        this.delay = delay;
    }

}
