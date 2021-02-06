package me.kosinkadink.performantplants.scripting.operations.action;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.ScriptType;
import me.kosinkadink.performantplants.scripting.storage.ScriptColor;
import me.kosinkadink.performantplants.util.EnumHelper;
import me.kosinkadink.performantplants.util.ScriptHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Consumer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScriptOperationAreaEffect extends ScriptOperationAction {

    private final List<PotionEffect> potionEffects;
    private final ScriptColor color;

    public ScriptOperationAreaEffect(List<PotionEffect> potionEffects, ScriptColor color,
                                     ScriptBlock duration, ScriptBlock durationOnUse,
                                     ScriptBlock particle,
                                     ScriptBlock radius, ScriptBlock radiusOnUse, ScriptBlock radiusPerTick,
                                     ScriptBlock reapplicationDelay) {
        super(duration, durationOnUse, particle, radius, radiusOnUse, radiusPerTick, reapplicationDelay);
        this.potionEffects = potionEffects;
        this.color = color;
    }

    public ScriptBlock getDuration() {
        return inputs[0];
    }
    public ScriptBlock getDurationOnUse() {
        return inputs[1];
    }
    public ScriptBlock getParticle() {
        return inputs[2];
    }
    public ScriptBlock getRadius() {
        return inputs[3];
    }
    public ScriptBlock getRadiusOnUse() {
        return inputs[4];
    }
    public ScriptBlock getRadiusPerTick() {
        return inputs[5];
    }
    public ScriptBlock getReapplicationDelay() {
        return inputs[6];
    }

    private Consumer<AreaEffectCloud> getConsumer(ExecutionContext context) {
        return cloud -> {
            for (PotionEffect potionEffect : potionEffects) {
                cloud.addCustomEffect(potionEffect, false);
            }
            if (color != null) {
                cloud.setColor(color.getColor(context));
            }
            Particle particle = EnumHelper.getParticle(getParticle().loadValue(context).getStringValue());
            if (particle != null) {
                cloud.setParticle(particle);
            }
            cloud.setDuration(getDuration().loadValue(context).getIntegerValue());
            cloud.setDurationOnUse(getDurationOnUse().loadValue(context).getIntegerValue());
            cloud.setRadius(getRadius().loadValue(context).getFloatValue());
            cloud.setRadiusOnUse(getRadiusOnUse().loadValue(context).getFloatValue());
            cloud.setRadiusPerTick(getRadiusPerTick().loadValue(context).getFloatValue());
            cloud.setReapplicationDelay(getReapplicationDelay().loadValue(context).getIntegerValue());
        };
    }


    @Nonnull
    @Override
    public ScriptResult perform(@Nonnull ExecutionContext context) throws IllegalArgumentException {
        if (context.isLocationPossible()) {
            Location location = context.getLocation();
            try {
                if (location.getWorld() != null) {
                    location.getWorld().spawn(location, AreaEffectCloud.class, getConsumer(context));
                    return ScriptResult.TRUE;
                }
            } catch (IllegalArgumentException e) {
                // do nothing just to avoid runtime exception
            }
        }
        return ScriptResult.FALSE;
    }

    @Override
    protected void setType() {
        type = ScriptType.BOOLEAN;
    }

    @Override
    protected void validateInputs() throws IllegalArgumentException {
        if (!ScriptHelper.isLong(getDuration())) {
            throw new IllegalArgumentException(String.format("duration must be ScriptType LONG, not %s", getDuration().getType()));
        }
        if (!ScriptHelper.isLong(getDurationOnUse())) {
            throw new IllegalArgumentException(String.format("duration-on-use must be ScriptType LONG, not %s", getDurationOnUse().getType()));
        }
        if (!ScriptHelper.isString(getParticle())) {
            throw new IllegalArgumentException(String.format("particle must be ScriptType STRING, not %s", getParticle().getType()));
        }
        if (!ScriptHelper.isNumeric(getRadius())) {
            throw new IllegalArgumentException(String.format("radius must be ScriptType LONG or DOUBLE, not %s", getRadius().getType()));
        }
        if (!ScriptHelper.isNumeric(getRadiusOnUse())) {
            throw new IllegalArgumentException(String.format("radius-on-use must be ScriptType LONG or DOUBLE, not %s", getRadiusOnUse().getType()));
        }
        if (!ScriptHelper.isNumeric(getRadiusPerTick())) {
            throw new IllegalArgumentException(String.format("radius-per-tick must be ScriptType LONG or DOUBLE, not %s", getRadiusPerTick().getType()));
        }
        if (!ScriptHelper.isLong(getReapplicationDelay())) {
            throw new IllegalArgumentException(String.format("reapplication-delay must be ScriptType LONG, not %s", getReapplicationDelay().getType()));
        }
    }
}
