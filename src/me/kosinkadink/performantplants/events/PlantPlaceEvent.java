package me.kosinkadink.performantplants.events;

import me.kosinkadink.performantplants.plants.Plant;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private Player player;
    private Plant plant;
    private Block block;

    public PlantPlaceEvent(Player player, Plant plant, Block block) {
        this.player = player;
        this.plant = plant;
        this.block = block;
    }

    public Player getPlayer() {
        return player;
    }

    public Plant getPlant() {
        return plant;
    }

    public Block getBlock() {
        return block;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
