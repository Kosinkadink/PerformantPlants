package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.scripting.ExecutionContext;
import me.kosinkadink.performantplants.scripting.ScriptBlock;
import me.kosinkadink.performantplants.scripting.ScriptResult;
import me.kosinkadink.performantplants.util.BlockHelper;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantSoundEffect extends PlantEffect {

    private ScriptBlock soundName = new ScriptResult("UI_BUTTON_CLICK");
    private ScriptBlock volume = new ScriptResult(1.0);
    private ScriptBlock pitch = new ScriptResult(1.0);
    private ScriptBlock offsetX = ScriptResult.ZERO;
    private ScriptBlock offsetY = ScriptResult.ZERO;
    private ScriptBlock offsetZ = ScriptResult.ZERO;
    private ScriptBlock multiplier = ScriptResult.ZERO;
    private ScriptBlock eyeLocation = ScriptResult.TRUE;
    private ScriptBlock ignoreDirectionY = ScriptResult.FALSE;
    private ScriptBlock clientSide = ScriptResult.FALSE;

    public PlantSoundEffect() { }

    @Override
    void performEffectActionPlayer(ExecutionContext context) {
        Location spawnLocation;
        Player player = context.getPlayer();
        // use eye location, if set
        if (isEyeLocation(context)) {
            spawnLocation = player.getEyeLocation();
        } else {
            spawnLocation = player.getLocation();
        }
        spawnLocation.add(
                getOffsetXValue(context),
                getOffsetYValue(context),
                getOffsetZValue(context)
        );
        // ignore direction y with multiplier, if set
        if (isIgnoreDirectionY(context)) {
            spawnLocation.add(player.getLocation().getDirection().setY(0).normalize().multiply(
                    getMultiplierValue(context)
            ));
        } else {
            spawnLocation.add(player.getLocation().getDirection().normalize().multiply(
                    getMultiplierValue(context)
            ));
        }
        // get particle value
        Sound sound = getSound(context);
        if (sound == null) {
            return;
        }
        // do client-side, if set
        if (isClientSide(context)) {
            player.playSound(spawnLocation, sound,
                    getVolumeValue(context),
                    getPitchValue(context)
            );
        } else {
            player.getWorld().playSound(spawnLocation, sound,
                    getVolumeValue(context),
                    getPitchValue(context)
            );
        }
    }

    @Override
    void performEffectActionBlock(ExecutionContext context) {
        Block block = context.getPlantBlock().getBlock();
        Location spawnLocation = BlockHelper.getCenter(block);
        // add offset
        spawnLocation.add(
                getOffsetXValue(context),
                getOffsetYValue(context),
                getOffsetZValue(context)
        );
        Sound sound = getSound(context);
        if (sound == null) {
            return;
        }
        block.getWorld().playSound(spawnLocation, sound,
                getVolumeValue(context),
                getPitchValue(context)
        );
    }

    public ScriptBlock getSoundName() {
        return soundName;
    }

    public Sound getSound(ExecutionContext context) {
        if (soundName == null) {
            return null;
        }
        try {
            return Sound.valueOf(soundName.loadValue(context).getStringValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setSoundName(ScriptBlock soundName) {
        this.soundName = soundName;
    }

    public ScriptBlock getVolume() {
        return volume;
    }

    public float getVolumeValue(ExecutionContext context) {
        return Math.max(0, volume.loadValue(context).getFloatValue());
    }

    public void setVolume(ScriptBlock volume) {
        this.volume = volume;
    }

    public ScriptBlock getPitch() {
        return pitch;
    }

    public float getPitchValue(ExecutionContext context) {
        return Math.max(0.5F,Math.min(2.0F, pitch.loadValue(context).getFloatValue()));
    }

    public void setPitch(ScriptBlock pitch) {
        this.pitch = pitch;
    }

    public ScriptBlock getOffsetX() {
        return offsetX;
    }

    public double getOffsetXValue(ExecutionContext context) {
        return offsetX.loadValue(context).getDoubleValue();
    }

    public void setOffsetX(ScriptBlock offsetX) {
        this.offsetX = offsetX;
    }

    public ScriptBlock getOffsetY() {
        return offsetY;
    }

    public double getOffsetYValue(ExecutionContext context) {
        return offsetY.loadValue(context).getDoubleValue();
    }

    public void setOffsetY(ScriptBlock offsetY) {
        this.offsetY = offsetY;
    }

    public ScriptBlock getOffsetZ() {
        return offsetZ;
    }

    public double getOffsetZValue(ExecutionContext context) {
        return offsetZ.loadValue(context).getDoubleValue();
    }

    public void setOffsetZ(ScriptBlock offsetZ) {
        this.offsetZ = offsetZ;
    }

    public ScriptBlock getMultiplier() {
        return multiplier;
    }

    public double getMultiplierValue(ExecutionContext context) {
        return multiplier.loadValue(context).getDoubleValue();
    }

    public void setMultiplier(ScriptBlock multiplier) {
        this.multiplier = multiplier;
    }

    public ScriptBlock getEyeLocation() {
        return eyeLocation;
    }

    public boolean isEyeLocation(ExecutionContext context) {
        return eyeLocation.loadValue(context).getBooleanValue();
    }

    public void setEyeLocation(ScriptBlock eyeLocation) {
        this.eyeLocation = eyeLocation;
    }

    public ScriptBlock getIgnoreDirectionY() {
        return ignoreDirectionY;
    }

    public boolean isIgnoreDirectionY(ExecutionContext context) {
        return ignoreDirectionY.loadValue(context).getBooleanValue();
    }

    public void setIgnoreDirectionY(ScriptBlock ignoreDirectionY) {
        this.ignoreDirectionY = ignoreDirectionY;
    }

    public ScriptBlock getClientSide() {
        return clientSide;
    }

    public boolean isClientSide(ExecutionContext context) {
        return clientSide.loadValue(context).getBooleanValue();
    }

    public void setClientSide(ScriptBlock clientSide) {
        this.clientSide = clientSide;
    }
}
