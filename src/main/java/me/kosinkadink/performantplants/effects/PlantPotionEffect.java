package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlantPotionEffect extends PlantEffect {

    private PotionEffectType potionEffectType;
    private int duration = 200;
    private int amplifier = 0;
    private boolean ambient = true;
    private boolean particles = false;
    private boolean icon = true;

    public PlantPotionEffect() { }

    public PlantPotionEffect(PotionEffectType potionEffectType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.potionEffectType = potionEffectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public PlantPotionEffect(PotionEffectType potionEffectType, int duration, int amplifier, boolean ambient, boolean particles) {
        this(potionEffectType, duration, amplifier, ambient, particles, true);
    }

    public PlantPotionEffect(PotionEffectType potionEffectType, int duration, int amplifier, boolean ambient) {
        this(potionEffectType, duration, amplifier, ambient, false, true);
    }

    public PlantPotionEffect(PotionEffectType potionEffectType, int duration, int amplifier) {
        this(potionEffectType, duration, amplifier, false, false, true);
    }

    @Override
    void performEffectAction(Player player, Location location) {
        player.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon));
    }

    @Override
    void performEffectAction(Block block) { }

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    public void setPotionEffectType(PotionEffectType potionEffectType) {
        this.potionEffectType = potionEffectType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public boolean getAmbient() {
        return ambient;
    }

    public void setAmbient(boolean ambient) {
        this.ambient = ambient;
    }

    public boolean getParticles() {
        return particles;
    }

    public void setParticles(boolean particles) {
        this.particles = particles;
    }

    public boolean getIcon() {
        return icon;
    }

    public void setIcon(boolean icon) {
        this.icon = icon;
    }

}
