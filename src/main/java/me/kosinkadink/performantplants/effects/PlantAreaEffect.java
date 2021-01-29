package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.scripting.storage.ScriptColor;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Consumer;

import java.util.ArrayList;

public class PlantAreaEffect extends PlantEffect {

    private final ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    private ScriptColor color = new ScriptColor();
    private ScriptBlock duration = new ScriptResult(200);
    private ScriptBlock durationOnUse = ScriptResult.ZERO;
    private ScriptBlock particleName = null;
    private ScriptBlock radius = new ScriptResult(1.0);
    private ScriptBlock radiusOnUse = ScriptResult.ZERO;
    private ScriptBlock radiusPerTick = ScriptResult.ZERO;
    private ScriptBlock reapplicationDelay = new ScriptResult(5);

    private Consumer<AreaEffectCloud> getConsumer(ExecutionContext context) {
        return cloud -> {
            for (PotionEffect potionEffect : potionEffects) {
                cloud.addCustomEffect(potionEffect, false);
            }
            if (color != null) {
                cloud.setColor(getColorValue(context));
            }
            Particle particle = getParticleValue(context);
            if (particle != null) {
                cloud.setParticle(particle);
            }
            cloud.setDuration(getDurationValue(context));
            cloud.setDurationOnUse(getDurationOnUseValue(context));
            cloud.setRadius(getRadiusValue(context));
            cloud.setRadiusOnUse(getRadiusOnUseValue(context));
            cloud.setRadiusPerTick(getRadiusPerTickValue(context));
            cloud.setReapplicationDelay(getReapplicationDelayValue(context));
        };
    }

    public PlantAreaEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        Player player = context.getPlayer();
        player.getWorld().spawn(player.getLocation(), AreaEffectCloud.class, getConsumer(context));
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        Block block = context.getPlantBlock().getBlock();
        block.getWorld().spawn(BlockHelper.getCenter(block), AreaEffectCloud.class, getConsumer(context));
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        potionEffects.add(potionEffect);
    }

    public ScriptColor getColor() {
        return color;
    }

    public Color getColorValue(ExecutionContext context) {
        return color.getColor(context);
    }

    public void setColor(ScriptColor color) {
        this.color = color;
    }

    public ScriptBlock getDuration() {
        return duration;
    }

    public int getDurationValue(ExecutionContext context) {
        return duration.loadValue(context).getIntegerValue();
    }

    public void setDuration(ScriptBlock duration) {
        this.duration = duration;
    }

    public ScriptBlock getDurationOnUse() {
        return durationOnUse;
    }

    public int getDurationOnUseValue(ExecutionContext context) {
        return durationOnUse.loadValue(context).getIntegerValue();
    }

    public void setDurationOnUse(ScriptBlock durationOnUse) {
        this.durationOnUse = durationOnUse;
    }

    public ScriptBlock getParticleName() {
        return particleName;
    }

    public Particle getParticleValue(ExecutionContext context) {
        if (particleName == null) {
            return null;
        }
        try {
            return Particle.valueOf(particleName.loadValue(context).getStringValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setParticleName(ScriptBlock particleName) {
        this.particleName = particleName;
    }

    public ScriptBlock getRadius() {
        return radius;
    }

    public float getRadiusValue(ExecutionContext context) {
        return radius.loadValue(context).getFloatValue();
    }

    public void setRadius(ScriptBlock radius) {
        this.radius = radius;
    }

    public ScriptBlock getRadiusOnUse() {
        return radiusOnUse;
    }

    public float getRadiusOnUseValue(ExecutionContext context) {
        return radiusOnUse.loadValue(context).getFloatValue();
    }

    public void setRadiusOnUse(ScriptBlock radiusOnUse) {
        this.radiusOnUse = radiusOnUse;
    }

    public ScriptBlock getRadiusPerTick() {
        return radiusPerTick;
    }

    public float getRadiusPerTickValue(ExecutionContext context) {
        return radiusPerTick.loadValue(context).getFloatValue();
    }

    public void setRadiusPerTick(ScriptBlock radiusPerTick) {
        this.radiusPerTick = radiusPerTick;
    }

    public ScriptBlock getReapplicationDelay() {
        return reapplicationDelay;
    }

    public int getReapplicationDelayValue(ExecutionContext context) {
        return reapplicationDelay.loadValue(context).getIntegerValue();
    }

    public void setReapplicationDelay(ScriptBlock reapplicationDelay) {
        this.reapplicationDelay = reapplicationDelay;
    }
}
