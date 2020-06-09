package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlantPotionEffect extends PlantEffect {

    private ScriptBlock potionEffectTypeName = new ScriptResult("UNLUCK");
    private ScriptBlock duration = new ScriptResult(200);
    private ScriptBlock amplifier = ScriptResult.ZERO;
    private ScriptBlock ambient = ScriptResult.TRUE;
    private ScriptBlock particles = ScriptResult.FALSE;
    private ScriptBlock icon = ScriptResult.TRUE;

    public PlantPotionEffect() { }

    @Override
    void performEffectAction(Player player, PlantBlock plantBlock) {
        PotionEffectType potionEffectType = getPotionEffectType(player, plantBlock);
        // if can't recognize type, do nothing
        if (potionEffectType == null) {
            return;
        }
        player.addPotionEffect(new PotionEffect(
                potionEffectType,
                getDurationValue(player, plantBlock),
                getAmplifierValue(player, plantBlock),
                isAmbient(player, plantBlock),
                isParticles(player, plantBlock),
                isIcon(player, plantBlock)
        ));
    }

    public ScriptBlock getPotionEffectTypeName() {
        return potionEffectTypeName;
    }

    public PotionEffectType getPotionEffectType(Player player, PlantBlock plantBlock) {
        return PotionEffectType.getByName(
                potionEffectTypeName.loadValue(plantBlock, player).getStringValue().toUpperCase()
        );
    }

    public void setPotionEffectTypeName(ScriptBlock potionEffectTypeName) {
        this.potionEffectTypeName = potionEffectTypeName;
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

    public ScriptBlock getAmplifier() {
        return amplifier;
    }

    public int getAmplifierValue(Player player, PlantBlock plantBlock) {
        return amplifier.loadValue(plantBlock, player).getIntegerValue()-1;
    }

    public void setAmplifier(ScriptBlock amplifier) {
        this.amplifier = amplifier;
    }

    public ScriptBlock getAmbient() {
        return ambient;
    }

    public boolean isAmbient(Player player, PlantBlock plantBlock) {
        return ambient.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setAmbient(ScriptBlock ambient) {
        this.ambient = ambient;
    }

    public ScriptBlock getParticles() {
        return particles;
    }

    public boolean isParticles(Player player, PlantBlock plantBlock) {
        return particles.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setParticles(ScriptBlock particles) {
        this.particles = particles;
    }

    public ScriptBlock getIcon() {
        return icon;
    }

    public boolean isIcon(Player player, PlantBlock plantBlock) {
        return icon.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setIcon(ScriptBlock icon) {
        this.icon = icon;
    }

}
