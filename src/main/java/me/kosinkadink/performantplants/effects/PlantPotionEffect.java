package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
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
    void performEffectActionPlayer(ExecutionContext context) {
        PotionEffectType potionEffectType = getPotionEffectType(context);
        // if can't recognize type, do nothing
        if (potionEffectType == null) {
            return;
        }
        context.getPlayer().addPotionEffect(new PotionEffect(
                potionEffectType,
                getDurationValue(context),
                getAmplifierValue(context),
                isAmbient(context),
                isParticles(context),
                isIcon(context)
        ));
    }

    public ScriptBlock getPotionEffectTypeName() {
        return potionEffectTypeName;
    }

    public PotionEffectType getPotionEffectType(ExecutionContext context) {
        return PotionEffectType.getByName(
                potionEffectTypeName.loadValue(context).getStringValue().toUpperCase()
        );
    }

    public void setPotionEffectTypeName(ScriptBlock potionEffectTypeName) {
        this.potionEffectTypeName = potionEffectTypeName;
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

    public ScriptBlock getAmplifier() {
        return amplifier;
    }

    public int getAmplifierValue(ExecutionContext context) {
        return amplifier.loadValue(context).getIntegerValue()-1;
    }

    public void setAmplifier(ScriptBlock amplifier) {
        this.amplifier = amplifier;
    }

    public ScriptBlock getAmbient() {
        return ambient;
    }

    public boolean isAmbient(ExecutionContext context) {
        return ambient.loadValue(context).getBooleanValue();
    }

    public void setAmbient(ScriptBlock ambient) {
        this.ambient = ambient;
    }

    public ScriptBlock getParticles() {
        return particles;
    }

    public boolean isParticles(ExecutionContext context) {
        return particles.loadValue(context).getBooleanValue();
    }

    public void setParticles(ScriptBlock particles) {
        this.particles = particles;
    }

    public ScriptBlock getIcon() {
        return icon;
    }

    public boolean isIcon(ExecutionContext context) {
        return icon.loadValue(context).getBooleanValue();
    }

    public void setIcon(ScriptBlock icon) {
        this.icon = icon;
    }

}
