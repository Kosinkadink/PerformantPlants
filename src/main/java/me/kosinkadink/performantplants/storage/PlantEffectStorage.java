package me.kosinkadink.performantplants.storage;

import me.kosinkadink.performantplants.effects.PlantEffect;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

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

    public void performEffectsDynamic(ExecutionContext context) {
        if (context.isPlayerSet()) {
            performEffectsPlayer(context);
        } else {
            performEffectsBlock(context);
        }
    }

    public void performEffectsPlayer(ExecutionContext context) {
        boolean limited = getEffectLimit() > 0;
        int effectCount = 0;
        for (PlantEffect effect : getEffects()) {
            boolean triggered = effect.performEffectPlayer(context);
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

    public void performEffectsBlock(ExecutionContext context) {
        boolean limited = getEffectLimit() > 0;
        int effectCount = 0;
        for (PlantEffect effect : getEffects()) {
            boolean triggered = effect.performEffectBlock(context);
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
