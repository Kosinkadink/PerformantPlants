package me.kosinkadink.performantplants.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface PlantEffect {

    void performEffect(Player player, Location location);
    void performEffect(Block block);

}
