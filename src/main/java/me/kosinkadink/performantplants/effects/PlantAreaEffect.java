package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
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

    private Consumer<AreaEffectCloud> getConsumer(Player player, PlantBlock plantBlock) {
        return cloud -> {
            for (PotionEffect potionEffect : potionEffects) {
                cloud.addCustomEffect(potionEffect, false);
            }
            if (color != null) {
                cloud.setColor(getColorValue(player, plantBlock));
            }
            Particle particle = getParticleValue(player, plantBlock);
            if (particle != null) {
                cloud.setParticle(particle);
            }
            cloud.setDuration(getDurationValue(player, plantBlock));
            cloud.setDurationOnUse(getDurationOnUseValue(player, plantBlock));
            cloud.setRadius(getRadiusValue(player, plantBlock));
            cloud.setRadiusOnUse(getRadiusOnUseValue(player, plantBlock));
            cloud.setRadiusPerTick(getRadiusPerTickValue(player, plantBlock));
            cloud.setReapplicationDelay(getReapplicationDelayValue(player, plantBlock));
        };
    }

    public PlantAreaEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.getWorld().spawn(player.getLocation(), AreaEffectCloud.class, getConsumer(player, plantBlock));
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        block.getWorld().spawn(BlockHelper.getCenter(block), AreaEffectCloud.class, getConsumer(null, plantBlock));
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        potionEffects.add(potionEffect);
    }

    public ScriptColor getColor() {
        return color;
    }

    public Color getColorValue(Player player, PlantBlock plantBlock) {
        return color.getColor(player, plantBlock);
    }

    public void setColor(ScriptColor color) {
        this.color = color;
    }

    public ScriptBlock getDuration() {
        return duration;
    }

    public int getDurationValue(Player player, PlantBlock plantBlock) {
        return duration.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setDuration(ScriptBlock duration) {
        this.duration = duration;
    }

    public ScriptBlock getDurationOnUse() {
        return durationOnUse;
    }

    public int getDurationOnUseValue(Player player, PlantBlock plantBlock) {
        return durationOnUse.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setDurationOnUse(ScriptBlock durationOnUse) {
        this.durationOnUse = durationOnUse;
    }

    public ScriptBlock getParticleName() {
        return particleName;
    }

    public Particle getParticleValue(Player player, PlantBlock plantBlock) {
        if (particleName == null) {
            return null;
        }
        try {
            return Particle.valueOf(particleName.loadValue(plantBlock, player).getStringValue().toUpperCase());
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

    public float getRadiusValue(Player player, PlantBlock plantBlock) {
        return radius.loadValue(plantBlock, player).getFloatValue();
    }

    public void setRadius(ScriptBlock radius) {
        this.radius = radius;
    }

    public ScriptBlock getRadiusOnUse() {
        return radiusOnUse;
    }

    public float getRadiusOnUseValue(Player player, PlantBlock plantBlock) {
        return radiusOnUse.loadValue(plantBlock, player).getFloatValue();
    }

    public void setRadiusOnUse(ScriptBlock radiusOnUse) {
        this.radiusOnUse = radiusOnUse;
    }

    public ScriptBlock getRadiusPerTick() {
        return radiusPerTick;
    }

    public float getRadiusPerTickValue(Player player, PlantBlock plantBlock) {
        return radiusPerTick.loadValue(plantBlock, player).getFloatValue();
    }

    public void setRadiusPerTick(ScriptBlock radiusPerTick) {
        this.radiusPerTick = radiusPerTick;
    }

    public ScriptBlock getReapplicationDelay() {
        return reapplicationDelay;
    }

    public int getReapplicationDelayValue(Player player, PlantBlock plantBlock) {
        return reapplicationDelay.loadValue(plantBlock, player).getIntegerValue();
    }

    public void setReapplicationDelay(ScriptBlock reapplicationDelay) {
        this.reapplicationDelay = reapplicationDelay;
    }
}
