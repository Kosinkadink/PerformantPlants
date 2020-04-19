package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantSoundEffect implements PlantEffect {

    private String sound;
    private float volume;
    private float pitch;
    private boolean clientside;

    public PlantSoundEffect(String sound, float volume, float pitch, boolean clientside) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.clientside = clientside;
    }

    @Override
    public void performEffect(Player player, Location location) {
        if (clientside) {
            player.playSound(player.getEyeLocation(), sound, volume, pitch);
        } else {
            player.getWorld().playSound(player.getEyeLocation(), sound, volume, pitch);
        }
    }

    @Override
    public void performEffect(Block block) {
        block.getWorld().playSound(block.getLocation(), sound, volume, pitch);
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isClientside() {
        return clientside;
    }

    public void setClientside(boolean clientside) {
        this.clientside = clientside;
    }
}
