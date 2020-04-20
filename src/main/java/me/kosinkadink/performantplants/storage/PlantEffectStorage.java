package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.effects.PlantEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PlantEffectStorage {

    private int effectLimit = -1;
    private ArrayList<PlantEffect> effects = new ArrayList<>();

    public PlantEffectStorage() { }

    public int getEffectLimit() {
        return effectLimit;
    }

    public void setEffectLimit(int effectLimit) {
        this.effectLimit = Math.max(0, effectLimit);
    }

    public void addEffect(PlantEffect effect) {
        effects.add(effect);
    }

    public ArrayList<PlantEffect> getEffects() {
        return effects;
    }

    public void performEffects(Player player, Location location) {
        boolean limited = getEffectLimit() > 0;
        int effectCount = 0;
        for (PlantEffect effect : getEffects()) {
            boolean triggered = effect.performEffect(player, location);
            // add to count if triggered
            if (triggered) {
                effectCount++;
                // if limit in place, break if limit has been reached
                if (limited && effectCount >= getEffectLimit()) {
                    break;
                }
            }
        }
    }

    public void performEffects(Block block) {
        boolean limited = getEffectLimit() > 0;
        int effectCount = 0;
        for (PlantEffect effect : getEffects()) {
            boolean triggered = effect.performEffect(block);
            // add to count if triggered
            if (triggered) {
                effectCount++;
                // if limit in place, break if limit has been reached
                if (limited && effectCount >= getEffectLimit()) {
                    break;
                }
            }
        }
    }

}
