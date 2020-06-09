package me.kosinkadink.performantplants.effects;

import me.kosinkadink.performantplants.blocks.PlantBlock;
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
    void performEffectAction(Player player, PlantBlock plantBlock) {
        Location spawnLocation;
        // use eye location, if set
        if (isEyeLocation(player, plantBlock)) {
            spawnLocation = player.getEyeLocation();
        } else {
            spawnLocation = player.getLocation();
        }
        spawnLocation.add(
                getOffsetXValue(player, plantBlock),
                getOffsetYValue(player, plantBlock),
                getOffsetZValue(player, plantBlock)
        );
        // ignore direction y with multiplier, if set
        if (isIgnoreDirectionY(player, plantBlock)) {
            spawnLocation.add(player.getLocation().getDirection().setY(0).normalize().multiply(
                    getMultiplierValue(player, plantBlock)
            ));
        } else {
            spawnLocation.add(player.getLocation().getDirection().normalize().multiply(
                    getMultiplierValue(player, plantBlock)
            ));
        }
        // get particle value
        Sound sound = getSound(player, plantBlock);
        if (sound == null) {
            return;
        }
        // do client-side, if set
        if (isClientSide(player, plantBlock)) {
            player.playSound(spawnLocation, sound,
                    getVolumeValue(player, plantBlock),
                    getPitchValue(player, plantBlock)
            );
        } else {
            player.getWorld().playSound(spawnLocation, sound,
                    getVolumeValue(player, plantBlock),
                    getPitchValue(player, plantBlock)
            );
        }
    }

    @Override
    void performEffectAction(Block block, PlantBlock plantBlock) {
        Location spawnLocation = BlockHelper.getCenter(block);
        // add offset
        spawnLocation.add(
                getOffsetXValue(null, plantBlock),
                getOffsetYValue(null, plantBlock),
                getOffsetZValue(null, plantBlock)
        );
        Sound sound = getSound(null, plantBlock);
        if (sound == null) {
            return;
        }
        block.getWorld().playSound(spawnLocation, sound,
                getVolumeValue(null, plantBlock),
                getPitchValue(null, plantBlock)
        );
    }

    public ScriptBlock getSoundName() {
        return soundName;
    }

    public Sound getSound(Player player, PlantBlock plantBlock) {
        if (soundName == null) {
            return null;
        }
        try {
            return Sound.valueOf(soundName.loadValue(plantBlock, player).getStringValue().toUpperCase());
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

    public float getVolumeValue(Player player, PlantBlock plantBlock) {
        return Math.max(0, volume.loadValue(plantBlock, player).getFloatValue());
    }

    public void setVolume(ScriptBlock volume) {
        this.volume = volume;
    }

    public ScriptBlock getPitch() {
        return pitch;
    }

    public float getPitchValue(Player player, PlantBlock plantBlock) {
        return Math.max(0.5F,Math.min(2.0F, pitch.loadValue(plantBlock, player).getFloatValue()));
    }

    public void setPitch(ScriptBlock pitch) {
        this.pitch = pitch;
    }

    public ScriptBlock getOffsetX() {
        return offsetX;
    }

    public double getOffsetXValue(Player player, PlantBlock plantBlock) {
        return offsetX.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetX(ScriptBlock offsetX) {
        this.offsetX = offsetX;
    }

    public ScriptBlock getOffsetY() {
        return offsetY;
    }

    public double getOffsetYValue(Player player, PlantBlock plantBlock) {
        return offsetY.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetY(ScriptBlock offsetY) {
        this.offsetY = offsetY;
    }

    public ScriptBlock getOffsetZ() {
        return offsetZ;
    }

    public double getOffsetZValue(Player player, PlantBlock plantBlock) {
        return offsetZ.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setOffsetZ(ScriptBlock offsetZ) {
        this.offsetZ = offsetZ;
    }

    public ScriptBlock getMultiplier() {
        return multiplier;
    }

    public double getMultiplierValue(Player player, PlantBlock plantBlock) {
        return multiplier.loadValue(plantBlock, player).getDoubleValue();
    }

    public void setMultiplier(ScriptBlock multiplier) {
        this.multiplier = multiplier;
    }

    public ScriptBlock getEyeLocation() {
        return eyeLocation;
    }

    public boolean isEyeLocation(Player player, PlantBlock plantBlock) {
        return eyeLocation.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setEyeLocation(ScriptBlock eyeLocation) {
        this.eyeLocation = eyeLocation;
    }

    public ScriptBlock getIgnoreDirectionY() {
        return ignoreDirectionY;
    }

    public boolean isIgnoreDirectionY(Player player, PlantBlock plantBlock) {
        return ignoreDirectionY.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setIgnoreDirectionY(ScriptBlock ignoreDirectionY) {
        this.ignoreDirectionY = ignoreDirectionY;
    }

    public ScriptBlock getClientSide() {
        return clientSide;
    }

    public boolean isClientSide(Player player, PlantBlock plantBlock) {
        return clientSide.loadValue(plantBlock, player).getBooleanValue();
    }

    public void setClientSide(ScriptBlock clientSide) {
        this.clientSide = clientSide;
    }
}
