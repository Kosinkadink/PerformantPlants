package me.kosinkadink.performantplants.scripting.operations.player;

import me.kosinkadink.performantplants.scripting.*;
import me.kosinkadink.performantplants.scripting.operations.type.ScriptOperationNoOptimize;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class ScriptOperationPotionEffect extends ScriptOperationNoOptimize {

    public ScriptOperationPotionEffect(ScriptBlock potionEffectType, ScriptBlock duration, ScriptBlock amplifier,
                                       ScriptBlock ambient, ScriptBlock particles, ScriptBlock icon) {
        super(potionEffectType, duration, amplifier, ambient, particles, icon);
    }

    public ScriptBlock getPotionEffectType() {
        return inputs[0];
    }

    public ScriptBlock getDuration() {
        return inputs[1];
    }

    public ScriptBlock getAmplifier() {
        return inputs[2];
    }

    public ScriptBlock getAmbient() {
        return inputs[3];
    }

    public ScriptBlock getParticles() {
        return inputs[4];
    }

    public ScriptBlock getIcon() {
        return inputs[5];
    }

    @Nonnull
    @Override
    public ScriptCategory getCategory() {
        return ScriptCategory.PLAYER;
    }

    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isPlayerSet()) {
            PotionEffectType potionEffectType = PotionEffectType.getByName(
                    getPotionEffectType().loadValue(context).getStringValue());
            if (potionEffectType == null) {
                return ScriptResult.FALSE;
            }
            context.getPlayer().addPotionEffect(new PotionEffect(
                    potionEffectType,
                    getDuration().loadValue(context).getIntegerValue(),
                    getAmplifier().loadValue(context).getIntegerValue(),
                    getAmbient().loadValue(context).getBooleanValue(),
                    getParticles().loadValue(context).getBooleanValue(),
                    getIcon().loadValue(context).getBooleanValue()
            ));
            return ScriptResult.TRUE;
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isString(getPotionEffectType())) {
            throw new IllegalArgumentException(String.format("potion must be ScriptType STRING, not %s",
                    getPotionEffectType().getType()));
        }
        if (!ScriptHelper.isLong(getDuration())) {
            throw new IllegalArgumentException(String.format("duration must be ScriptType LONG, not %s",
                    getPotionEffectType().getType()));
        }
        if (!ScriptHelper.isLong(getAmplifier())) {
            throw new IllegalArgumentException(String.format("amplifier must be ScriptType LONG, not %s",
                    getPotionEffectType().getType()));
        }
        if (!ScriptHelper.isBoolean(getAmbient())) {
            throw new IllegalArgumentException(String.format("ambient must be ScriptType BOOLEAN, not %s",
                    getPotionEffectType().getType()));
        }
        if (!ScriptHelper.isBoolean(getParticles())) {
            throw new IllegalArgumentException(String.format("particles must be ScriptType BOOLEAN, not %s",
                    getPotionEffectType().getType()));
        }
        if (!ScriptHelper.isBoolean(getIcon())) {
            throw new IllegalArgumentException(String.format("icon must be ScriptType BOOLEAN, not %s",
                    getPotionEffectType().getType()));
        }
    }
}
