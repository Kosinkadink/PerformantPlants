package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantSoundEffect extends PlantEffect {

    //private String sound;
    private Sound sound;
    private float volume = 1;
    private float pitch = 1;
    private boolean clientSide = true;

    public PlantSoundEffect() { }

    @Override
    void performEffectAction(Player player, Location location) {
        if (clientSide) {
            player.playSound(player.getEyeLocation(), sound, volume, pitch);
        } else {
            player.getWorld().playSound(player.getEyeLocation(), sound, volume, pitch);
        }
    }

    @Override
    void performEffectAction(Block block) {
        block.getWorld().playSound(block.getLocation(), sound, volume, pitch);
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0, volume);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = Math.max(0.5F,Math.min(2.0F, pitch));
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }
}
