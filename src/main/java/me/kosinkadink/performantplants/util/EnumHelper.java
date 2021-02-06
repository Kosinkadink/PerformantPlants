package me.kosinkadink.performantplants.util;

import org.bukkit.Particle;
import org.bukkit.Sound;

public class EnumHelper {

    public static Sound getSound(String name) {
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Particle getParticle(String name) {
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
