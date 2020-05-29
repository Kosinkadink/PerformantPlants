package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.util.RandomHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class PlantEffect {

    protected double chance = 100.0;
    protected int delay = 0;

    public boolean performEffect(Player player, PlantBlock plantBlock) {
        if (RandomHelper.generateChancePercentage(chance)) {
            if (delay == 0) {
                performEffectAction(player, plantBlock);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectAction(player, plantBlock),
                            delay
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
        if (RandomHelper.generateChancePercentage(chance)) {
            if (delay == 0) {
                performEffectAction(block, plantBlock);
            } else {
                Plugin pp = Bukkit.getPluginManager().getPlugin("performantplants");
                if (pp != null) {
                    Bukkit.getScheduler().runTaskLater(pp,
                            () -> performEffectAction(block, plantBlock),
                            delay
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

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = Math.min(100, Math.max(0, chance));
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = Math.max(0, delay);
    }

}
