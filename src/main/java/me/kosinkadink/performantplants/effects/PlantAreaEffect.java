package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
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

    private ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    private Color color;
    private int duration = 200;
    private int durationOnUse = 0;
    private Particle particle;
    private float radius = 1;
    private float radiusOnUse = 0;
    private float radiusPerTick = 0;
    private int reapplicationDelay = 5;

    Consumer<AreaEffectCloud> consumer = new Consumer<AreaEffectCloud>() {
        @Override
        public void accept(AreaEffectCloud cloud) {
            for (PotionEffect potionEffect : potionEffects) {
                cloud.addCustomEffect(potionEffect, false);
            }
            if (color != null) {
                cloud.setColor(color);
            }
            if (particle != null) {
                cloud.setParticle(particle);
            }
            cloud.setDuration(duration);
            cloud.setDurationOnUse(durationOnUse);
            cloud.setRadius(radius);
            cloud.setRadiusOnUse(radiusOnUse);
            cloud.setRadiusPerTick(radiusPerTick);
            cloud.setReapplicationDelay(reapplicationDelay);
        }
    };

    public PlantAreaEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        player.getWorld().spawn(player.getLocation(), AreaEffectCloud.class, consumer);
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        block.getWorld().spawn(BlockHelper.getCenter(block), AreaEffectCloud.class, consumer);
    }

    public void addPotionEffect(PotionEffect potionEffect) {
        potionEffects.add(potionEffect);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDurationOnUse() {
        return durationOnUse;
    }

    public void setDurationOnUse(int durationOnUse) {
        this.durationOnUse = durationOnUse;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadiusOnUse() {
        return radiusOnUse;
    }

    public void setRadiusOnUse(float radiusOnUse) {
        this.radiusOnUse = radiusOnUse;
    }

    public float getRadiusPerTick() {
        return radiusPerTick;
    }

    public void setRadiusPerTick(float radiusPerTick) {
        this.radiusPerTick = radiusPerTick;
    }

    public int getReapplicationDelay() {
        return reapplicationDelay;
    }

    public void setReapplicationDelay(int reapplicationDelay) {
        this.reapplicationDelay = reapplicationDelay;
    }
}
